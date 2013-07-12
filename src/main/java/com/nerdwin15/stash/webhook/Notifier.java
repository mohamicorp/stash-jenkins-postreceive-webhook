package com.nerdwin15.stash.webhook;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.stash.hook.repository.RepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.ssh.api.SshCloneUrlResolver;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.nerdwin15.stash.webhook.service.HttpClientFactory;

/**
 * Service object that does the actual notification.
 * 
 * @author Michael Irwin (mikesir87)
 * @author Peter Leibiger (kuhnroyal)
 */
public class Notifier {

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
   * Field name for the Clone type property
   */
  public static final String CLONE_TYPE = "cloneType";
  
  /**
   * Field name for the username field when using HTTP-based repo cloning
   */
  public static final String HTTP_USERNAME = "httpUserName";

  /**
   * Field name for the ignore certs property
   */
  public static final String IGNORE_CERTS = "ignoreCerts";

  /**
   * Field name for the ignore committers property
   */
	public static final String IGNORE_COMMITTERS = "ignoreCommitters";

  private static final Logger LOGGER = 
      LoggerFactory.getLogger(Notifier.class);
  private static final String URL = "%s/git/notifyCommit?url=%s";

  private final NavBuilder navBuilder;
  private final RepositoryHookService hookService;
  private final HttpClientFactory httpClientFactory;
  private final SshCloneUrlResolver sshCloneUrlResolver;

  /**
   * Create a new instance
   * @param navBuilder NavBuilder to help build out URL
   * @param hookService Hook service
   * @param httpClientFactory Factory to generate HttpClients
   * @param sshCloneUrlResolver Utility to get ssh clone urls
   */
  public Notifier(NavBuilder navBuilder, 
		      RepositoryHookService hookService, 
          HttpClientFactory httpClientFactory,
          SshCloneUrlResolver sshCloneUrlResolver) {
	  
    this.navBuilder = navBuilder;
    this.hookService = hookService;
    this.httpClientFactory = httpClientFactory;
    this.sshCloneUrlResolver = sshCloneUrlResolver;
  }

  /**
   * Send notification to Jenkins for the provided repository.
   * @param repository The repository to base the notification on.
   * @return Text result from Jenkins
   */
  public @Nullable String notify(@Nonnull Repository repository) {
    final RepositoryHook hook = hookService.getByKey(repository, KEY);
    final Settings settings = hookService.getSettings(repository, KEY);
    if (hook == null || !hook.isEnabled() || settings == null) {
      LOGGER.debug("Hook not configured correctly or not enabled, returning.");
      return null;
    }

    return notify(repository, settings.getString(JENKINS_BASE), 
        settings.getBoolean(IGNORE_CERTS, false),
        settings.getString(CLONE_TYPE),
        settings.getString(HTTP_USERNAME));
  }

  /**
   * Send notification to Jenkins using the provided settings
   * @param repository The repository to base the notification on.
   * @param jenkinsBase Base URL for Jenkins instance
   * @param ignoreCerts True if all certs should be allowed
   * @param cloneType The repository type used for cloning (http or ssh)
   * @param httpUsername The username used if using http-based cloning
   * @return The response body for the notification.
   */
  public @Nullable String notify(@Nonnull Repository repository, 
      String jenkinsBase, boolean ignoreCerts, String cloneType,
      String httpUsername) {
    
    HttpClient client = null;
    final String url = getUrl(repository, maybeReplaceSlash(jenkinsBase),
    		cloneType, httpUsername);

    try {
      client = httpClientFactory.getHttpClient(url.startsWith("https"), 
          ignoreCerts);

      HttpResponse response = client.execute(new HttpGet(url));
      LOGGER.debug("Successfully triggered jenkins with url '{}': ", url);
      InputStream content = response.getEntity().getContent();
      return CharStreams.toString(
          new InputStreamReader(content, Charsets.UTF_8));
    } catch (Exception e) {
      LOGGER.error("Error triggering jenkins with url '" + url + "'", e);
    } finally {
      if (client != null) {
        client.getConnectionManager().shutdown();
        LOGGER.debug("Successfully shutdown connection");
      }
    }
    return null;
  }

  /**
   * Get the url for notifying of Jenkins. Protected for testing purposes
   * @param repository The repository to base the request to.
   * @param jenkinsBase The base URL of the Jenkins instance
   * @param cloneType The clone type being used for the Git repo
   * @param httpUsername The username used if using HTTP cloning
   * @return The url to use for notifying Jenkins
   */
  protected String getUrl(Repository repository, String jenkinsBase, 
  		String cloneType, String httpUsername) {
    String repoUrl = getRepoUrl(repository, cloneType, httpUsername);
    return String.format(URL, jenkinsBase, urlEncode(repoUrl));
  }
  
  /**
   * Get the URL used for repository cloning, based on the provided settings
   * @param repository The repository to clone
   * @param cloneType The clone type being used (ssh or http)
   * @param httpUsername The username to be used, if using HTTP cloning.
   * @return The clone URL for the repository, based on provided settings
   */
  protected String getRepoUrl(Repository repository, String cloneType, 
  		String httpUsername) {
  	if (cloneType.equals("ssh"))
  		return sshCloneUrlResolver.getCloneUrl(repository);
  	if (httpUsername == null || httpUsername.isEmpty())
  		return navBuilder.repo(repository).clone("git")
  				.buildAbsoluteWithoutUsername();
  	String url  = navBuilder.repo(repository).clone("git")
				.buildAbsoluteWithoutUsername();
  	return url.replace("://", "://" + httpUsername + "@");
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