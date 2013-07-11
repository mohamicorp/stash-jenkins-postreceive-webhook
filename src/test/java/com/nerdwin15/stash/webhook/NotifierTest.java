package com.nerdwin15.stash.webhook;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.atlassian.stash.hook.repository.RepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.ssh.api.SshCloneUrlResolver;
import com.nerdwin15.stash.webhook.service.HttpClientFactory;

/**
 * Test for the Notifier class
 * 
 * @author Peter Leibiger (kuhnroyal)
 * @author Michael Irwin (mikesir87)
 */
public class NotifierTest {

  private static final String HTTP_SOME_STASH_COM = "http://some.stash.com";
  private static final String FOOBAR_REPO = "/foo/bar.git";
  private static final String JENKINS_BASE_URL = "http://localhost.jenkins";
  private static final String SSH_URL = 
  		"ssh://git@some.stash.com:7999/hello/world.git";

  private NavBuilder navBuilder;
  private RepositoryHookService hookService;
  private HttpClientFactory httpClientFactory;
  private HttpClient httpClient;
  private ClientConnectionManager connectionManager;
  private SshCloneUrlResolver sshCloneUrlResolver;
  private Repository repo;
  private RepositoryHook repoHook;
  private NavBuilder.Repo navRepo;
  private NavBuilder.RepoClone navRepoClone;
  private Settings settings;
  private Notifier notifier;

  /**
   * Setup tasks
   */
  @Before
  public void setup() throws Exception {
    navBuilder = mock(NavBuilder.class);
    hookService = mock(RepositoryHookService.class);
    httpClientFactory = mock(HttpClientFactory.class);
    sshCloneUrlResolver = mock(SshCloneUrlResolver.class);
    notifier = new Notifier(navBuilder, hookService, httpClientFactory, 
    		sshCloneUrlResolver);

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
    when(httpClientFactory
        .getHttpClient(any(Boolean.class), any(Boolean.class)))
        .thenReturn(httpClient);
    when(httpClient.getConnectionManager()).thenReturn(connectionManager);
    
    when(sshCloneUrlResolver.getCloneUrl(repo)).thenReturn(SSH_URL);

    when(navBuilder.repo(repo)).thenReturn(navRepo);
    when(navRepo.clone("git")).thenReturn(navRepoClone);
    when(navRepoClone.buildAbsoluteWithoutUsername())
      .thenReturn(HTTP_SOME_STASH_COM + "/scm" + FOOBAR_REPO);

    when(settings.getString(Notifier.JENKINS_BASE))
      .thenReturn(JENKINS_BASE_URL);
    when(settings.getString(Notifier.CLONE_TYPE)).thenReturn("ssh");
    when(settings.getString(Notifier.HTTP_USERNAME)).thenReturn(null);
    when(settings.getBoolean(Notifier.IGNORE_CERTS, false)).thenReturn(false);
  }

  /**
   * Validates nothing happens if the hook isn't found
   * @throws Exception
   */
  @Test
  public void shouldReturnEarlyWhenHookIsNull() throws Exception {
    when(hookService.getByKey(repo, Notifier.KEY)).thenReturn(null);
    notifier.notify(repo);
    verify(httpClientFactory, never())
      .getHttpClient(anyBoolean(), anyBoolean());
  }

  /**
   * Validates that nothing happens if the hook is disabled
   * @throws Exception
   */
  @Test
  public void shouldReturnEarlyWhenHookIsNotEnabled() throws Exception {
    when(repoHook.isEnabled()).thenReturn(false);
    notifier.notify(repo);
    verify(httpClientFactory, never())
        .getHttpClient(anyBoolean(), anyBoolean());
  }

  /**
   * Validates that nothing happens if the settings aren't set properly
   * @throws Exception
   */
  @Test
  public void shouldReturnEarlyWhenSettingsAreNull() throws Exception {
    when(hookService.getSettings(repo, Notifier.KEY)).thenReturn(null);
    notifier.notify(repo);
    verify(httpClientFactory, never())
      .getHttpClient(anyBoolean(), anyBoolean());
  }

  /**
   * Validates the URL is correct when using a non-SSL path
   * @throws Exception
   */
  @Test
  public void shouldCallTheCorrectUrlWithoutSsl() throws Exception {
    notifier.notify(repo);

    ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);

    verify(httpClientFactory, times(1)).getHttpClient(false, false);
    verify(httpClient, times(1)).execute(captor.capture());
    verify(connectionManager, times(1)).shutdown();

    assertEquals("http://localhost.jenkins/git/notifyCommit?" 
    		+ "url=http%3A%2F%2Fsome.stash.com%2Fscm%2Ffoo%2Fbar.git",
        captor.getValue().getURI().toString());
  }

  /**
   * Validates the path is correct when using a SSL path
   * @throws Exception
   */
  @Test
  public void shouldCallTheCorrectUrlWithSsl() throws Exception {
    when(settings.getString(Notifier.JENKINS_BASE))
      .thenReturn(JENKINS_BASE_URL.replace("http", "https"));

    notifier.notify(repo);

    ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);

    verify(httpClientFactory, times(1)).getHttpClient(true, false);
    verify(httpClient, times(1)).execute(captor.capture());
    verify(connectionManager, times(1)).shutdown();

    assertEquals("https://localhost.jenkins/git/notifyCommit?" 
    		+ "url=http%3A%2F%2Fsome.stash.com%2Fscm%2Ffoo%2Fbar.git",
        captor.getValue().getURI().toString());
  }

  /**
   * Validates that the correct path is taken when using SSL but ignoring cert
   * validation.
   * @throws Exception
   */
  @Test
  public void shouldCallTheCorrectUrlWithSslAndIgnoreCerts() throws Exception {
    when(settings.getString(Notifier.JENKINS_BASE))
      .thenReturn(JENKINS_BASE_URL.replace("http", "https"));
    when(settings.getBoolean(Notifier.IGNORE_CERTS, false)).thenReturn(true);

    notifier.notify(repo);

    ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);

    verify(httpClientFactory, times(1)).getHttpClient(true, true);
    verify(httpClient, times(1)).execute(captor.capture());
    verify(connectionManager, times(1)).shutdown();

    assertEquals("https://localhost.jenkins/git/notifyCommit?"
        + "url=http%3A%2F%2Fsome.stash.com%2Fscm%2Ffoo%2Fbar.git",
        captor.getValue().getURI().toString());
  }

  /**
   * Validates that the correct path is used, even when a trailing slash
   * is provided on the Jenkins Base URL
   * @throws Exception
   */
  @Test
  public void shouldCallTheCorrectUrlWithTrailingSlashOnJenkinsBaseUrl() 
      throws Exception {
    when(settings.getString(Notifier.JENKINS_BASE))
      .thenReturn(JENKINS_BASE_URL.concat("/"));

    notifier.notify(repo);

    ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);

    verify(httpClientFactory, times(1)).getHttpClient(false, false);
    verify(httpClient, times(1)).execute(captor.capture());
    verify(connectionManager, times(1)).shutdown();

    assertEquals("http://localhost.jenkins/git/notifyCommit?"
        + "url=http%3A%2F%2Fsome.stash.com%2Fscm%2Ffoo%2Fbar.git",
        captor.getValue().getURI().toString());
  }
  
  /**
   * Verify that the correct url is used when using a ssh clone type
   * @throws Exception
   */
  @Test
  public void verifyCorrectUrlUsingSshCloneType() throws Exception {
  	when(settings.getString(Notifier.CLONE_TYPE)).thenReturn("git");
  	
    notifier.notify(repo);

    ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);

    verify(httpClientFactory, times(1)).getHttpClient(false, false);
    verify(httpClient, times(1)).execute(captor.capture());
    verify(connectionManager, times(1)).shutdown();

    assertEquals("http://localhost.jenkins/git/notifyCommit?"
        + "url=ssh%3A%2F%2Fgit%40some.stash.com%3A7999%2Fhello%2Fworld.git",
        captor.getValue().getURI().toString());  	
  }
  
  /**
   * Verify that the correct url is used when providing an empty HTTP username
   * @throws Exception
   */
  @Test
  public void verifyCorrectUrlUsingEmptyHttpUsername() throws Exception {
  	when(settings.getString(Notifier.HTTP_USERNAME)).thenReturn("");
  	
    notifier.notify(repo);

    ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);

    verify(httpClientFactory, times(1)).getHttpClient(false, false);
    verify(httpClient, times(1)).execute(captor.capture());
    verify(connectionManager, times(1)).shutdown();

    assertEquals("http://localhost.jenkins/git/notifyCommit?"
        + "url=http%3A%2F%2Fsome.stash.com%2Fscm%2Ffoo%2Fbar.git",
        captor.getValue().getURI().toString());  	
  }
  
  /**
   * Verify that the correct url is used when providing a custom HTTP username
   * @throws Exception
   */
  @Test
  public void verifyCorrectUrlUsingCustomHttpUsername() throws Exception {
  	when(settings.getString(Notifier.HTTP_USERNAME)).thenReturn("user");
  	
    notifier.notify(repo);

    ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);

    verify(httpClientFactory, times(1)).getHttpClient(false, false);
    verify(httpClient, times(1)).execute(captor.capture());
    verify(connectionManager, times(1)).shutdown();

    assertEquals("http://localhost.jenkins/git/notifyCommit?"
        + "url=http%3A%2F%2Fuser%40some.stash.com%2Fscm%2Ffoo%2Fbar.git",
        captor.getValue().getURI().toString());  	
  }
  
  
}
