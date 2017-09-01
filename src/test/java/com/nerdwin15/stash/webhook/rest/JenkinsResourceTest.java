package com.nerdwin15.stash.webhook.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.util.Map;

import javax.ws.rs.core.Response;

import com.atlassian.bitbucket.scm.http.HttpScmProtocol;
import com.atlassian.bitbucket.scm.ssh.SshScmProtocol;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.repository.Branch;
import com.atlassian.bitbucket.repository.RefService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.ssh.SshConfiguration;
import com.atlassian.bitbucket.ssh.SshConfigurationService;
import com.atlassian.bitbucket.permission.PermissionValidationService;
import com.nerdwin15.stash.webhook.NotificationResult;
import com.nerdwin15.stash.webhook.Notifier;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test case for the JenkinsResource class.
 *
 * @author Michael Irwin (mikesir87)
 */
public class JenkinsResourceTest {

  private static final String JENKINS_BASE = "http://jenkins.localhost/jenkins";
  private static final String CLONE_TYPE = "http";
  private static final boolean IGNORE_CERTS = false;
  private static final boolean OMIT_HASH_CODE = false;
  private static final boolean OMIT_BRANCH_NAME = false;
  private static final boolean OMIT_TARGET_BRANCH = false;

  private static final String HTTP_URL =
      "https://stash.localhost/stash/scm/test/test.git";
  private static final String SSH_URL =
      "ssh://git@stash.localhost:7999/test/test.git";
  private static final String EMPTY_SSH_URL = "";

  private JenkinsResource resource;
  private Notifier notifier;
  private PermissionValidationService permissionValidationService;
  private I18nService i18nService;
  private SshConfigurationService sshConfigurationService;
  private SshScmProtocol sshScmProtocol;
  private HttpScmProtocol httpScmProtocol;
  private RefService refService;

  private Repository repository;

  /**
   * Setup tasks
   */
  @Before
  public void setup() throws Exception {
    notifier = mock(Notifier.class);
    permissionValidationService = mock(PermissionValidationService.class);
    i18nService = mock(I18nService.class);
    sshConfigurationService = mock(SshConfigurationService.class);

    sshScmProtocol = mock(SshScmProtocol.class);
    httpScmProtocol = mock(HttpScmProtocol.class);

    refService = mock(RefService.class);

    resource = new JenkinsResource(notifier, permissionValidationService,
        i18nService, sshConfigurationService, sshScmProtocol, httpScmProtocol,
        refService);

    repository = mock(Repository.class);
    Project project = mock(Project.class);
    when(repository.getProject()).thenReturn(project);
    when(project.getKey()).thenReturn("KEY");
    when(repository.getSlug()).thenReturn("SLUG");
  }

  /**
   * Use latest commit on default branch for test-trigger
   */
  @Test
  public void shouldUseDefaultBranchToTest() {
    Branch defaultBranch = mock(Branch.class);
    when(refService.getDefaultBranch(repository)).thenReturn(defaultBranch);
    when(defaultBranch.getDisplayId()).thenReturn("master");
    when(defaultBranch.getLatestCommit()).thenReturn("anySha1OfLatestCommit");

    NotificationResult notificationResult = mock(NotificationResult.class);
    when(notifier.notify(repository, JENKINS_BASE, IGNORE_CERTS,
      CLONE_TYPE, HTTP_URL, "master", "anySha1OfLatestCommit", "master",
      OMIT_HASH_CODE, OMIT_BRANCH_NAME, OMIT_TARGET_BRANCH)).thenReturn(notificationResult);
    when(notificationResult.isSuccessful()).thenReturn(true);

    Map<String, Object> result =
      resource.test(repository, JENKINS_BASE, CLONE_TYPE, HTTP_URL,
        IGNORE_CERTS, OMIT_HASH_CODE, OMIT_BRANCH_NAME, OMIT_TARGET_BRANCH);
    assertTrue((Boolean) result.get("successful"));
  }

  /**
   * Validate that if a null JenkinsBase is provided, a BAD_REQUEST is returned.
   */
  @Test
  public void shouldFailWhenJenkinsBaseNullProvidedToTest() {
    Map<String, Object> result =
        resource.test(repository, null, CLONE_TYPE, null, IGNORE_CERTS,
          OMIT_HASH_CODE, OMIT_BRANCH_NAME, OMIT_TARGET_BRANCH);
    assertFalse((Boolean) result.get("successful"));
  }

  /**
   * Validate that if a null CloneType is provided, a BAD_REQUEST is returned.
   */
  @Test
  public void shouldFailWhenCloneTypeNullProvidedToTest() {
    Map<String, Object> result =
        resource.test(repository, JENKINS_BASE, null, HTTP_URL, IGNORE_CERTS,
          OMIT_HASH_CODE,OMIT_BRANCH_NAME, OMIT_TARGET_BRANCH);
    assertFalse((Boolean) result.get("successful"));
  }

  /**
   * Validate that if a null repo clone url is provided when the clone type
   * is set to CUSTOM, then a BAD_REQUEST is returned.
   */
  @Test
  public void shouldFailWhenCloneUrlNullProvidedToTest() {
    Map<String, Object> result =
        resource.test(repository, JENKINS_BASE, "custom", null, IGNORE_CERTS,
          OMIT_HASH_CODE, OMIT_BRANCH_NAME, OMIT_TARGET_BRANCH);
    assertFalse((Boolean) result.get("successful"));
  }

  /**
   * Validate that the config endpoint creates the expected results.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testConfigResource() {
    when(httpScmProtocol.getCloneUrl(repository, null)).thenReturn(HTTP_URL);

    SshConfiguration sshConfiguration = mock(SshConfiguration.class);

    when(sshConfigurationService.getConfiguration()).thenReturn(sshConfiguration);
    when(sshConfiguration.isEnabled()).thenReturn(true);
    when(sshScmProtocol.getCloneUrl(repository, null)).thenReturn(SSH_URL);

    Response response = resource.config(repository);
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    Map<String, String> data = (Map<String, String>) response.getEntity();
    assertEquals(data.get("ssh"), SSH_URL);
    assertEquals(data.get("http"), HTTP_URL);

    verify(sshScmProtocol).getCloneUrl(repository, null);
    verify(httpScmProtocol).getCloneUrl(repository, null);
  }

  /**
   * Validate that the config endpoint safely handles the case when the internal SSH
   * server is disabled in Stash 3.0 and later.
   */
  @Test
  public void shouldNotProduceExceptionWhenSshDisabled() {
    when(httpScmProtocol.getCloneUrl(repository, null)).thenReturn(HTTP_URL);

    SshConfiguration sshConfiguration = mock(SshConfiguration.class);

    when(sshConfigurationService.getConfiguration()).thenReturn(sshConfiguration);
    when(sshConfiguration.isEnabled()).thenReturn(false);
    when(sshScmProtocol.getCloneUrl(repository, null)).thenThrow(
            new IllegalStateException("Internal SSH server is disabled"));

    Response response = resource.config(repository);
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    Map<String, String> data = (Map<String, String>) response.getEntity();
    assertEquals(data.get("ssh"), EMPTY_SSH_URL);
    assertEquals(data.get("http"), HTTP_URL);

    verify(sshScmProtocol, never()).getCloneUrl(repository, null);
    verify(httpScmProtocol).getCloneUrl(repository, null);
  }

}
