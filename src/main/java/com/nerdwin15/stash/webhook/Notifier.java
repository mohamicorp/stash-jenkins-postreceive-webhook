package com.nerdwin15.stash.webhook;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.util.concurrent.ThreadFactories;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.stash.hook.repository.RepositoryHook;
import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.ssh.api.SshCloneUrlResolver;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.SecurityService;
import com.atlassian.stash.util.UncheckedOperation;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.nerdwin15.stash.webhook.service.HttpClientFactory;
import com.nerdwin15.stash.webhook.service.SettingsService;
import org.springframework.beans.factory.DisposableBean;

/**
 * Service object that does the actual notification.
 * 
 * @author Michael Irwin (mikesir87)
 * @author Peter Leibiger (kuhnroyal)
 */
public class Notifier implements DisposableBean {

  /**
   * Key for the repository hook
   */
  public static final String KEY = 
      "com.nerdwin15.stash-stash-webhook-jenkins:jenkinsPostReceiveHook";
  
  /**
   * Field name for the Jenkins base URL property
   */
  public static final String JENKINS_BASE = "jenkinsBase";

  /**
   * Field name for the Repo clone type property
   */
  public static final String CLONE_TYPE = "cloneType";

  /**
   * Field name for the Repo Clone Url property
   */
  public static final String CLONE_URL = "gitRepoUrl";
  
  /**
   * Field name for the ignore certs property
   */
  public static final String IGNORE_CERTS = "ignoreCerts";

  /**
   * Field name for the omit hash code property
   */
  public static final String OMIT_HASH_CODE = "omitHashCode";

  /**
   * Field name for the omit branch name property
   */
  public static final String OMIT_BRANCH_NAME = "omitBranchName";

  /**
   * Field name for the ignore committers property
   */
  public static final String IGNORE_COMMITTERS = "ignoreCommitters";

  /**
   * Field name for the branch options selection property
   */
  public static final String BRANCH_OPTIONS = "branchOptions";

  /**
   * Field name for the branch options branches property
   */
  public static final String BRANCH_OPTIONS_BRANCHES = "branchOptionsBranches";

  private static final Logger LOGGER = 
      LoggerFactory.getLogger(Notifier.class);
  private static final String BASE_URL = "%s/git/notifyCommit?url=%s";
  private static final String HASH_URL_PARAMETER = "&sha1=%s";
  private static final String BRANCH_URL_PARAMETER = "&branches=%s";

  private final HttpClientFactory httpClientFactory;
  private final SettingsService settingsService;
  private final ExecutorService executorService;
  private final NavBuilder navBuilder;
  private final SecurityService securityService;
  private final SshCloneUrlResolver sshCloneUrlResolver;

  /**
   * Create a new instance
   * @param settingsService Service used to get webhook settings
   * @param httpClientFactory Factory to generate HttpClients
   * @param navBuilder navBuilder to generate http urls
   * @param securityService securityService
   * @param sshCloneUrlResolver ssh clone URL resolver
   */
  public Notifier(SettingsService settingsService,
      HttpClientFactory httpClientFactory,
      NavBuilder navBuilder,
      SecurityService securityService,
      SshCloneUrlResolver sshCloneUrlResolver) {
    
    this.httpClientFactory = httpClientFactory;
    this.settingsService = settingsService;
    this.executorService = Executors.newCachedThreadPool(ThreadFactories.namedThreadFactory("JenkinsWebhook", ThreadFactories.Type.DAEMON));
    this.navBuilder = navBuilder;
    this.securityService = securityService;
    this.sshCloneUrlResolver = sshCloneUrlResolver;
  }

  /**
   * Send notification to Jenkins for the provided repository on a background thread.
   * This is better when running as a background task, to release the calling thread.
   * @param repo The repository to base the notification on.
   * @param strRef The branch ref related to the commit
   * @param strSha1 The commit's SHA1 hash code.
   * @return A future of the text result from Jenkins
   */
  @Nonnull
  public Future<NotificationResult> notifyBackground(@Nonnull final Repository repo, //CHECKSTYLE:annot
      final String strRef, final String strSha1) {
    return executorService.submit(new Callable<NotificationResult>() {
      @Override
      public NotificationResult call() throws Exception {
        return Notifier.this.notify(repo, strRef, strSha1);
      }
    });
  }

  /**
   * Send notification to Jenkins for the provided repository.
   * @param repo The repository to base the notification on.
   * @param strRef The branch ref related to the commit
   * @param strSha1 The commit's SHA1 hash code.
   * @return Text result from Jenkins
   */
  public @Nullable NotificationResult notify(@Nonnull Repository repo, //CHECKSTYLE:annot
      String strRef, String strSha1) {
    final RepositoryHook hook = settingsService.getRepositoryHook(repo);
    final Settings settings = settingsService.getSettings(repo);
    if (hook == null || !hook.isEnabled() || settings == null) {
      LOGGER.debug("Hook not configured correctly or not enabled, returning.");
      return null;
    }

    return notify(repo, settings.getString(JENKINS_BASE), 
        settings.getBoolean(IGNORE_CERTS, false),
        settings.getString(CLONE_TYPE),
        settings.getString(CLONE_URL),
        strRef, strSha1,
        settings.getBoolean(OMIT_HASH_CODE, false),
        settings.getBoolean(OMIT_BRANCH_NAME, false));
  }

  /**
   * Send notification to Jenkins using the provided settings
   * @param repo The repository to base the notification on.
   * @param jenkinsBase Base URL for Jenkins instance
   * @param ignoreCerts True if all certs should be allowed
   * @param cloneType The repository type
   * @param cloneUrl The repository url
   * @param strRef The branch ref related to the commit
   * @param strSha1 The commit's SHA1 hash code.
   * @param omitHashCode Defines whether the commit's SHA1 hash code is omitted
   *        in notification to Jenkins.
   * @param omitBranchName Defines whether the commit's branch name is omitted
   * @return The notification result.
   */
  public @Nullable NotificationResult notify(@Nonnull Repository repo, //CHECKSTYLE:annot
      String jenkinsBase, boolean ignoreCerts, String cloneType, String cloneUrl,
      String strRef, String strSha1, boolean omitHashCode, boolean omitBranchName) {
    
    HttpClient client = null;
    String url;

    try {
        url = getUrl(repo, maybeReplaceSlash(jenkinsBase),
            cloneType, cloneUrl, strRef, strSha1, omitHashCode, omitBranchName);
    } catch (Exception e) {
        LOGGER.error("Error getting Jenkins URL", e);
        return new NotificationResult(false, null, e.getMessage());
    }

    try {
      client = httpClientFactory.getHttpClient(url.startsWith("https"), 
          ignoreCerts);

      HttpResponse response = client.execute(new HttpGet(url));
      LOGGER.debug("Successfully triggered jenkins with url '{}': ", url);
      InputStream content = response.getEntity().getContent();
      String responseBody =  CharStreams.toString(
          new InputStreamReader(content, Charsets.UTF_8));
      boolean successful = responseBody.startsWith("Scheduled");
      
      NotificationResult result = new NotificationResult(successful, url, 
              "Jenkins response: " + responseBody);
      return result;
    } catch (Exception e) {
      LOGGER.error("Error triggering jenkins with url '" + url + "'", e);
      return new NotificationResult(false, url, e.getMessage());
    } finally {
      if (client != null) {
        client.getConnectionManager().shutdown();
        LOGGER.debug("Successfully shutdown connection");
      }
    }
  }

  @Override
  public void destroy() {
    executorService.shutdownNow();
  }

  /**
   * Get the url for notifying of Jenkins. Protected for testing purposes
   * @param repository The repository to base the request to.
   * @param jenkinsBase The base URL of the Jenkins instance
   * @param cloneType The type used to clone the repository
   * @param customCloneUrl The url used for cloning the repository
   * @param strRef The branch ref related to the commit
   * @param strSha1 The commit's SHA1 hash code.
   * @param omitHashCode Defines whether the commit's SHA1 hash code is omitted
   *        in notification to Jenkins.
   * @return The url to use for notifying Jenkins
   */
  protected String getUrl(final Repository repository, final String jenkinsBase,
      final String cloneType, final String customCloneUrl, final String strRef, final String strSha1, boolean omitHashCode, boolean omitBranchName) {
      String cloneUrl = customCloneUrl;
    // Older installs won't have a cloneType value - treat as custom
    if (cloneType != null && !cloneType.equals("custom")) {
        if (cloneType.equals("http")) {
            cloneUrl = navBuilder.repo(repository).clone("git")
                .buildAbsoluteWithoutUsername();
        } else if (cloneType.equals("ssh")) {
            // The user just pushed to the repo, so must have had access
            cloneUrl = securityService.doWithPermission("Retrieving SSH clone url", Permission.REPO_READ, new UncheckedOperation<String>() {
                @Override
                public String perform() {
                    return sshCloneUrlResolver.getCloneUrl(repository);
                }
            });
        } else {
            LOGGER.error("Unknown cloneType: {}", cloneType);
            throw new RuntimeException("Unknown cloneType: " + cloneType);
        }
    }

    StringBuilder url = new StringBuilder();
    url.append(String.format(BASE_URL, jenkinsBase, urlEncode(cloneUrl)));

    if(strRef != null && !omitBranchName)
      url.append(String.format(BRANCH_URL_PARAMETER, strRef));
    if(!omitHashCode)
      url.append(String.format(HASH_URL_PARAMETER, strSha1));

    return url.toString();
  }

  private static String urlEncode(String string) {
    try {
      return URLEncoder.encode(string, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private String maybeReplaceSlash(String string) {
    return string == null ? null : string.replaceFirst("/$", "");
  }
}
