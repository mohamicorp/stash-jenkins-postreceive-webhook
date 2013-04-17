package com.nerdwin15.stash.webhook;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.setting.Settings;
import com.nerdwin15.stash.webhook.service.HttpClientFactory;

/**
 * Test case for the {@link JenkinsWebhook} class.
 * 
 * @author Michael Irwin
 */
public class JenkinsWebhookTest {

	private String jenkinsBase;
	private String gitRepoUrl;
	private Boolean ignoreCertValidation;

	private Settings settings;
	private RepositoryHookContext context;
	private HttpClientFactory factory;
	private HttpClient client;
	private ClientConnectionManager connectionManager;
	
	private JenkinsWebhook hook;
	
	@Before
	public void setup() throws Exception {
		hook = new JenkinsWebhook();

		jenkinsBase = "http://localhost.jenkins";
		gitRepoUrl = "ssh://git@some.stash.com:7999/test/helloworld.git";
		ignoreCertValidation = false;
	
		settings = mock(Settings.class);
		context = mock(RepositoryHookContext.class);
		when(context.getSettings()).thenReturn(settings);
		when(settings.getString("jenkinsBase")).thenReturn(jenkinsBase);
		when(settings.getString("gitRepoUrl")).thenReturn(gitRepoUrl);
		when(settings.getBoolean("ignoreCerts")).thenReturn(ignoreCertValidation);

		factory = mock(HttpClientFactory.class);
		hook.setFactory(factory);
		
		client = mock(HttpClient.class);
		when(factory.getHttpClient(any(Boolean.class), any(Boolean.class)))
			.thenReturn(client);
		connectionManager = mock(ClientConnectionManager.class);
		when(client.getConnectionManager()).thenReturn(connectionManager);
	}
	
	@Test
	public void validateUrlPath() throws Exception {
		String expected = "http://localhost.jenkins/git/notifyCommit?url="
				+ "ssh%3A%2F%2Fgit%40some.stash.com%3A7999%2Ftest%2Fhelloworld.git";
		assertEquals(expected, hook.getUrl(jenkinsBase, gitRepoUrl));
		assertEquals(expected, hook.getUrl(jenkinsBase + "/", gitRepoUrl));
	}
	
	@Test
	public void validateShouldntSendWithMissingBaseUrl() throws Exception {
		when(settings.getString("jenkinsBase")).thenReturn(null);
		hook.postReceive(context, null);
		verify(factory, times(0)).getHttpClient(any(Boolean.class), any(Boolean.class));
	}
	
	@Test
	public void validateShouldntSendWithMissingGitUrl() throws Exception {
		when(settings.getString("gitRepoUrl")).thenReturn(null);
		hook.postReceive(context, null);
		verify(factory, times(0)).getHttpClient(any(Boolean.class), any(Boolean.class));
	}
	
	@Test
	public void exerciseSendingNotificationNonSsl() throws Exception {
		hook.postReceive(context, null);
		
		verify(factory, times(1)).getHttpClient(false, ignoreCertValidation);
		verify(client, times(1)).execute(any(HttpGet.class));
	}

	@Test
	public void exerciseSendingNotificationUsingSsl() throws Exception {
		jenkinsBase = jenkinsBase.replace("http:", "https:");
		when(settings.getString("jenkinsBase")).thenReturn(jenkinsBase);
		
		hook.postReceive(context, null);
		
		verify(factory, times(1)).getHttpClient(true, ignoreCertValidation);
		verify(client, times(1)).execute(any(HttpGet.class));
	}

	@Test
	public void exerciseSendingNotificationUsingSslAndTrustAll() throws Exception {
		jenkinsBase = jenkinsBase.replace("http:", "https:");
		when(settings.getString("jenkinsBase")).thenReturn(jenkinsBase);
		
		ignoreCertValidation = true;
		when(settings.getBoolean("ignoreCerts")).thenReturn(ignoreCertValidation);
		
		hook.postReceive(context, null);
		
		verify(factory, times(1)).getHttpClient(true, true);
		verify(client, times(1)).execute(any(HttpGet.class));
	}

}
