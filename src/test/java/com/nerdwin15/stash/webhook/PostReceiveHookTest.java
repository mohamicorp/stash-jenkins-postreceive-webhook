package com.nerdwin15.stash.webhook;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.stash.event.RepositoryEvent;
import com.atlassian.stash.event.RepositoryPushEvent;
import com.atlassian.stash.event.StashEvent;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;
import com.atlassian.stash.user.StashUser;
import com.nerdwin15.stash.webhook.service.eligibility.EligibilityFilterChain;

/**
 * Test case for the PostReceiveHook class.
 * 
 * @author Peter Leibiger (kuhnroyal)
 * @author Michael Irwin (mikesir87)
 */
public class PostReceiveHookTest {

  private static final String JENKINS_BASE_URL = "http://localhost.jenkins";
  private static final String CLONE_TYPE_HTTP = "http";
  private static final String CLONE_TYPE_SSH = "ssh";

  private Notifier notifier;
  private PostReceiveHook hook;
  private SettingsValidationErrors errors;
  private Settings settings;
  private Repository repo;
  private EligibilityFilterChain eligibilityFilter;
  
  /**
   * Setup tasks
   */
  @Before
  public void setup() throws Exception {
    eligibilityFilter = mock(EligibilityFilterChain.class);
    notifier = mock(Notifier.class);
    hook = new PostReceiveHook(eligibilityFilter, notifier);
    settings = mock(Settings.class);
    errors = mock(SettingsValidationErrors.class);
    repo = mock(Repository.class);

    when(eligibilityFilter
        .shouldDeliverNotification(any(RepositoryEvent.class)))
        .thenReturn(Boolean.TRUE);
    
    when(settings.getString(Notifier.JENKINS_BASE))
      .thenReturn(JENKINS_BASE_URL);
    when(settings.getString(Notifier.CLONE_TYPE)).thenReturn(CLONE_TYPE_HTTP);
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
   * Validates that the hook delegates to the Notifier to do actual notifying.
   * @throws Exception
   */
  @Test
  public void shouldDeliverWhenChainIsOk() throws Exception {
    RepositoryPushEvent event = getEvent("doesntMatter");
    hook.onPushEvent(event);
    verify(notifier).notify(repo);
  }

  /**
   * Validates that the hook does not deliver the message if the eligibility
   * filter chain determines the notification shouldn't be sent.
   * @throws Exception
   */
  @Test
  public void shouldDeliverWhenChainSaysNo() throws Exception {
    RepositoryPushEvent event = getEvent("doesntMatter");
    when(eligibilityFilter.shouldDeliverNotification(event)).thenReturn(false);
    hook.onPushEvent(event);
    verify(notifier, never()).notify(repo);
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
   * Validate that an error is added when the repo clone type is empty
   * @throws Exception
   */
  @Test
  public void shouldAddErrorWhenCloneTypeEmpty() throws Exception {
    when(settings.getString(Notifier.CLONE_TYPE)).thenReturn("");
    hook.validate(settings, errors, repo);
    verify(errors).addFieldError(eq(Notifier.CLONE_TYPE), anyString());
  }

  /**
   * Validate that an error is added when the repo clone type is empty
   * @throws Exception
   */
  @Test
  public void shouldAddErrorWhenCloneTypeInvalid() throws Exception {
    when(settings.getString(Notifier.CLONE_TYPE)).thenReturn("fake_type");
    hook.validate(settings, errors, repo);
    verify(errors).addFieldError(eq(Notifier.CLONE_TYPE), anyString());
  }

  /**
   * Validate that an error is added when the repo clone type is empty
   * @throws Exception
   */
  @Test
  public void shouldAddErrorWhenCloneTypeIsSsh() throws Exception {
    when(settings.getString(Notifier.CLONE_TYPE)).thenReturn(CLONE_TYPE_SSH);
    hook.validate(settings, errors, repo);
    verify(errors, never()).addFieldError(anyString(), anyString());
  }
  
  private RepositoryPushEvent getEvent(String username) throws Exception {
    RepositoryPushEvent event = new RepositoryPushEvent("TEST", repo, 
        new ArrayList<RefChange>());
    
    StashUser user = mock(StashUser.class);
    Field field = StashEvent.class.getDeclaredField("user");
    field.setAccessible(true);
    field.set(event, user);
    
    when(user.getName()).thenReturn(username);
    return event;
  }

}
