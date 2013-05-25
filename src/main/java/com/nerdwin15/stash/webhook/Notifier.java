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

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.stash.hook.repository.RepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
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
   * Field name for the Stash base URL property
   */
  public static final String STASH_BASE = "stashBase";

  /**
   * Field name for the ignore certs property
   */
  public static final String IGNORE_CERTS = "ignoreCerts";

  private static final Logger LOGGER = 
      LoggerFactory.getLogger(Notifier.class);
  private static final String URL = "%s/git/notifyCommit?url=%s";

  private final NavBuilder navBuilder;
  private final ApplicationProperties applicationProperties;
  private final RepositoryHookService hookService;
  private final HttpClientFactory httpClientFactory;

  /**
   * Create a new instance
   * @param navBuilder NavBuilder to help build out URL
   * @param applicationProperties Ability to get application properties
   * @param hookService Hook service
   * @param httpClientFactory Factory to generate HttpClients
   */
  public Notifier(NavBuilder navBuilder, 
		  ApplicationProperties applicationProperties,
          RepositoryHookService hookService, 
          HttpClientFactory httpClientFactory) {
	  
    this.navBuilder = navBuilder;
    this.applicationProperties = applicationProperties;
    this.hookService = hookService;
    this.httpClientFactory = httpClientFactory;
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
        settings.getString(STASH_BASE), 
        settings.getBoolean(IGNORE_CERTS, false));
  }

  /**
   * Send notification to Jenkins using the provided settings
   * @param repository The repository to base the notification on.
   * @param jenkinsBase Base URL for Jenkins instance
   * @param stashBase Optional overridden value for stash base.
   * @param ignoreCerts True if all certs should be allowed
   * @return The response body for the notification.
   */
  public @Nullable String notify(@Nonnull Repository repository, 
      String jenkinsBase, String stashBase, boolean ignoreCerts) {
    
    HttpClient client = null;
    final String url = getUrl(repository, maybeReplaceSlash(jenkinsBase), 
        maybeReplaceSlash(stashBase));

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
   * @param stashBase An optional overridden value for base URL of the 
   * repository
   * @return The url to use for notifying Jenkins
   */
  protected String getUrl(Repository repository, String jenkinsBase, 
      String stashBase) {
    
    String repoUrl = 
        navBuilder.repo(repository).clone("git").buildAbsoluteWithoutUsername();
    
    if (!Strings.isNullOrEmpty(stashBase)) {
      repoUrl = repoUrl.replace(applicationProperties.getBaseUrl(), 
          maybeReplaceSlash(stashBase));
      if (stashBase.startsWith("ssh")) {
        repoUrl = repoUrl.replace("scm/", "");
      }
    }
    return String.format(URL, jenkinsBase, urlEncode(repoUrl));
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