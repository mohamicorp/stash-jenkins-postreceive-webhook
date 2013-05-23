package com.nerdwin15.stash.webhook;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.stash.hook.repository.RepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.google.common.base.Strings;
import com.nerdwin15.stash.webhook.service.HttpClientFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Notifier {

    public static final String KEY = "com.nerdwin15.stash-stash-webhook-jenkins:jenkinsPostReceiveHook";
    public static final String JENKINS_BASE = "jenkinsBase";
    public static final String STASH_BASE = "stashBase";
    public static final String IGNORE_CERTS = "ignoreCerts";

    private static final Logger LOGGER = LoggerFactory.getLogger(Notifier.class);
    private static final String URL = "%s/git/notifyCommit?url=%s";

    private final NavBuilder navBuilder;

    private final ApplicationProperties applicationProperties;

    private final RepositoryHookService hookService;

    private final HttpClientFactory httpClientFactory;

    public Notifier(NavBuilder navBuilder, ApplicationProperties applicationProperties,
                    RepositoryHookService hookService, HttpClientFactory httpClientFactory) {
        this.navBuilder = navBuilder;
        this.applicationProperties = applicationProperties;
        this.hookService = hookService;
        this.httpClientFactory = httpClientFactory;
    }

    public void notify(@Nonnull Repository repository) {
        final RepositoryHook hook = hookService.getByKey(repository, KEY);
        final Settings settings = hookService.getSettings(repository, KEY);
        if (hook == null || !hook.isEnabled() || settings == null) {
            LOGGER.debug("Hook not configured correctly or not enabled, returning.");
            return;
        }

        HttpClient client = null;
        final String url = getUrl(repository, settings);
        try {
            client = httpClientFactory.getHttpClient(url.startsWith("https"), settings.getBoolean(IGNORE_CERTS, false));
            client.execute(new HttpGet(url));
            LOGGER.debug("Successfully triggered jenkins with url '{}': ", url);
        } catch (Exception e) {
            LOGGER.error("Error triggering jenkins with url '" + url + "'", e);
        } finally {
            if (client != null) {
                client.getConnectionManager().shutdown();
                LOGGER.debug("Successfully shutdown connection");
            }
        }
    }

    private String getUrl(Repository repository, Settings settings) {
        final String jenkinsUrl = settings.getString(JENKINS_BASE).replaceFirst("/$", "");
        final String stashBaseUrl = settings.getString(STASH_BASE);
        String repoUrl = navBuilder.repo(repository).clone("git").buildAbsoluteWithoutUsername();
        if (!Strings.isNullOrEmpty(stashBaseUrl)) {
            repoUrl = repoUrl.replace(applicationProperties.getBaseUrl(), stashBaseUrl.replaceFirst("/$", ""));
            if (stashBaseUrl.indexOf("ssh") == 0) {
            	repoUrl = repoUrl.replace("scm/", "");
            }
        }
        return String.format(URL, jenkinsUrl, urlEncode(repoUrl));
    }

    private static String urlEncode(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}