package com.nerdwin15.stash.webhook;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.stash.hook.repository.RepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.nerdwin15.stash.webhook.service.HttpClientFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class NotifierTest {

    private static final String HTTP_SOME_STASH_COM = "http://some.stash.com";
    private static final String FOOBAR_REPO = "/foo/bar.git";
    private static final String JENKINS_BASE_URL = "http://localhost.jenkins";
    private static final String STASH_BASE_URL = "ssh://git@some.stash.com:7999";

    private NavBuilder navBuilder;

    private ApplicationProperties applicationProperties;

    private RepositoryHookService hookService;

    private HttpClientFactory httpClientFactory;

    private HttpClient httpClient;

    private ClientConnectionManager connectionManager;

    private Repository repo;

    private RepositoryHook repoHook;

    private NavBuilder.Repo navRepo;

    private NavBuilder.RepoClone navRepoClone;

    private Settings settings;

    private Notifier notifier;

    @Before
    public void setup() throws Exception {
        navBuilder = mock(NavBuilder.class);
        applicationProperties = mock(ApplicationProperties.class);
        hookService = mock(RepositoryHookService.class);
        httpClientFactory = mock(HttpClientFactory.class);
        notifier = new Notifier(navBuilder, applicationProperties, hookService, httpClientFactory);

        repo = mock(Repository.class);
        repoHook = mock(RepositoryHook.class);
        settings = mock(Settings.class);
        httpClient = mock(HttpClient.class);
        connectionManager = mock(ClientConnectionManager.class);

        navRepo = mock(NavBuilder.Repo.class);
        navRepoClone = mock(NavBuilder.RepoClone.class);

        when(repoHook.isEnabled()).thenReturn(true);
        when(hookService.getByKey(repo, Notifier.KEY)).thenReturn(repoHook);
        when(hookService.getSettings(repo, Notifier.KEY)).thenReturn(settings);
        when(httpClientFactory.getHttpClient(any(Boolean.class), any(Boolean.class))).thenReturn(httpClient);
        connectionManager = mock(ClientConnectionManager.class);
        when(httpClient.getConnectionManager()).thenReturn(connectionManager);

        when(navBuilder.repo(repo)).thenReturn(navRepo);
        when(navRepo.clone("git")).thenReturn(navRepoClone);
        when(navRepoClone.buildAbsoluteWithoutUsername()).thenReturn(HTTP_SOME_STASH_COM + FOOBAR_REPO);

        when(applicationProperties.getBaseUrl()).thenReturn(HTTP_SOME_STASH_COM);

        when(settings.getString(Notifier.JENKINS_BASE)).thenReturn(JENKINS_BASE_URL);
        when(settings.getString(Notifier.STASH_BASE)).thenReturn(null);
        when(settings.getBoolean(Notifier.IGNORE_CERTS, false)).thenReturn(false);
    }

    @Test
    public void shouldReturnEarlyWhenHookIsNull() throws Exception {
        when(hookService.getByKey(repo, Notifier.KEY)).thenReturn(null);
        notifier.notify(repo);
        verify(httpClientFactory, never()).getHttpClient(anyBoolean(), anyBoolean());
    }

    @Test
    public void shouldReturnEarlyWhenHookIsNotEnabled() throws Exception {
        when(repoHook.isEnabled()).thenReturn(false);
        notifier.notify(repo);
        verify(httpClientFactory, never()).getHttpClient(anyBoolean(), anyBoolean());
    }

    @Test
    public void shouldReturnEarlyWhenSettingsAreNull() throws Exception {
        when(hookService.getSettings(repo, Notifier.KEY)).thenReturn(null);
        notifier.notify(repo);
        verify(httpClientFactory, never()).getHttpClient(anyBoolean(), anyBoolean());
    }

    @Test
    public void shouldCallTheCorrectUrlWithoutSsl() throws Exception {
        notifier.notify(repo);

        ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);

        verify(httpClientFactory, times(1)).getHttpClient(false, false);
        verify(httpClient, times(1)).execute(captor.capture());
        verify(connectionManager, times(1)).shutdown();

        assertEquals("http://localhost.jenkins/git/notifyCommit?url=http%3A%2F%2Fsome.stash.com%2Ffoo%2Fbar.git",
                captor.getValue().getURI().toString());
    }

    @Test
    public void shouldCallTheCorrectUrlWithSsl() throws Exception {
        when(settings.getString(Notifier.JENKINS_BASE)).thenReturn(JENKINS_BASE_URL.replace("http", "https"));

        notifier.notify(repo);

        ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);

        verify(httpClientFactory, times(1)).getHttpClient(true, false);
        verify(httpClient, times(1)).execute(captor.capture());
        verify(connectionManager, times(1)).shutdown();

        assertEquals("https://localhost.jenkins/git/notifyCommit?url=http%3A%2F%2Fsome.stash.com%2Ffoo%2Fbar.git",
                captor.getValue().getURI().toString());
    }

    @Test
    public void shouldCallTheCorrectUrlWithSslAndIgnoreCerts() throws Exception {
        when(settings.getString(Notifier.JENKINS_BASE)).thenReturn(JENKINS_BASE_URL.replace("http", "https"));
        when(settings.getBoolean(Notifier.IGNORE_CERTS, false)).thenReturn(true);

        notifier.notify(repo);

        ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);

        verify(httpClientFactory, times(1)).getHttpClient(true, true);
        verify(httpClient, times(1)).execute(captor.capture());
        verify(connectionManager, times(1)).shutdown();

        assertEquals("https://localhost.jenkins/git/notifyCommit?url=http%3A%2F%2Fsome.stash.com%2Ffoo%2Fbar.git",
                captor.getValue().getURI().toString());
    }

    @Test
    public void shouldCallTheCorrectUrlWithTrailingSlashOnJenkinsBaseUrl() throws Exception {
        when(settings.getString(Notifier.JENKINS_BASE)).thenReturn(JENKINS_BASE_URL.concat("/"));

        notifier.notify(repo);

        ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);

        verify(httpClientFactory, times(1)).getHttpClient(false, false);
        verify(httpClient, times(1)).execute(captor.capture());
        verify(connectionManager, times(1)).shutdown();

        assertEquals("http://localhost.jenkins/git/notifyCommit?url=http%3A%2F%2Fsome.stash.com%2Ffoo%2Fbar.git",
                captor.getValue().getURI().toString());
    }

    @Test
    public void shouldCallTheCorrectUrlWithCustomStashBaseUrl() throws Exception {
        when(settings.getString(Notifier.STASH_BASE)).thenReturn(STASH_BASE_URL);

        notifier.notify(repo);

        ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);

        verify(httpClientFactory, times(1)).getHttpClient(false, false);
        verify(httpClient, times(1)).execute(captor.capture());
        verify(connectionManager, times(1)).shutdown();

        assertEquals("http://localhost.jenkins/git/notifyCommit?url=ssh%3A%2F%2Fgit%40some.stash.com%3A7999%2Ffoo%2Fbar.git",
                captor.getValue().getURI().toString());
    }

    @Test
    public void shouldCallTheCorrectUrlWithAndCustomStashBaseUrlAndTrailingSlash() throws Exception {
        when(settings.getString(Notifier.STASH_BASE)).thenReturn(STASH_BASE_URL.concat("/"));

        notifier.notify(repo);

        ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);

        verify(httpClientFactory, times(1)).getHttpClient(false, false);
        verify(httpClient, times(1)).execute(captor.capture());
        verify(connectionManager, times(1)).shutdown();

        assertEquals("http://localhost.jenkins/git/notifyCommit?url=ssh%3A%2F%2Fgit%40some.stash.com%3A7999%2Ffoo%2Fbar.git",
                captor.getValue().getURI().toString());
    }
}
