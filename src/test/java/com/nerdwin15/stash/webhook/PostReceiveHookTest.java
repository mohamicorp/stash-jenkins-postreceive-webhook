package com.nerdwin15.stash.webhook;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;

/**
 * Test case for the PostReceiveHook class.
 * 
 * @author Peter Leibiger (kuhnroyal)
 * @author Michael Irwin (mikesir87)
 */
public class PostReceiveHookTest {

  private static final String JENKINS_BASE_URL = "http://localhost.jenkins";
  private static final String CLONE_URL = 
      "http://some.stash.com/scm/foo/bar.git";

  private PostReceiveHook hook;
  private SettingsValidationErrors errors;
  private Settings settings;
  private Repository repo;
  
  /**
   * Setup tasks
   */
  @Before
  public void setup() throws Exception {
    hook = new PostReceiveHook();
    settings = mock(Settings.class);
    errors = mock(SettingsValidationErrors.class);
    repo = mock(Repository.class);

    when(settings.getString(Notifier.JENKINS_BASE))
      .thenReturn(JENKINS_BASE_URL);
    when(settings.getString(Notifier.CLONE_URL)).thenReturn(CLONE_URL);
  }

  /**
   * Validates that the hook itself does nothing.
   * @throws Exception
   */
  @Test
  public void shouldPostReceiveDoesNothing() throws Exception {
    RepositoryHookContext ctx = mock(RepositoryHookContext.class);
    hook.postReceive(ctx, null);
  }

  /**
   * Validate that an error is added when the Jenkins Base parameter is null
   * @throws Exception
   */
  @Test
  public void shouldAddErrorWhenJenkinsBaseNull() throws Exception {
    when(settings.getString(Notifier.JENKINS_BASE)).thenReturn(null);
    hook.validate(settings, errors, repo);
    verify(errors).addFieldError(eq(Notifier.JENKINS_BASE), anyString());
  }

  /**
   * Validate that an error is added when the repo clone type is null
   * @throws Exception
   */
  @Test
  public void shouldAddErrorWhenCloneTypeNull() throws Exception {
    when(settings.getString(Notifier.CLONE_TYPE)).thenReturn(null);
    hook.validate(settings, errors, repo);
    verify(errors).addFieldError(eq(Notifier.CLONE_TYPE), anyString());
  }

 /**
   * Validate that an error is added when the repo clone type is invalid
   * @throws Exception
   */
  @Test
  public void shouldAddErrorWhenCloneTypeInvalid() throws Exception {
    when(settings.getString(Notifier.CLONE_TYPE)).thenReturn("invalid");
    hook.validate(settings, errors, repo);
    verify(errors).addFieldError(eq(Notifier.CLONE_TYPE), anyString());
  }

  /**
   * Validate that an error is added when the repo clone url is null
   * @throws Exception
   */
  @Test
  public void shouldAddErrorWhenCloneUrlNull() throws Exception {
    when(settings.getString(Notifier.CLONE_URL)).thenReturn(null);
    hook.validate(settings, errors, repo);
    verify(errors).addFieldError(eq(Notifier.CLONE_URL), anyString());
  }

  /**
   * Validate that an error is added when the repo clone url is empty
   * @throws Exception
   */
  @Test
  public void shouldAddErrorWhenCloneUrlEmpty() throws Exception {
    when(settings.getString(Notifier.CLONE_URL)).thenReturn("");
    hook.validate(settings, errors, repo);
    verify(errors).addFieldError(eq(Notifier.CLONE_URL), anyString());
  }

}
