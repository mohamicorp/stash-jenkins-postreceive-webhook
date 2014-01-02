package com.nerdwin15.stash.webhook;

import com.atlassian.stash.event.RepositoryRefsChangedEvent;
import com.atlassian.stash.event.pull.PullRequestOpenedEvent;
import com.atlassian.stash.event.pull.PullRequestReopenedEvent;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestRef;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.user.StashUser;
import com.nerdwin15.stash.webhook.service.SettingsService;
import com.nerdwin15.stash.webhook.service.eligibility.EligibilityFilterChain;
import com.nerdwin15.stash.webhook.service.eligibility.EventContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test case for the PullRequestRescopeListener class.
 *
 * @author Melvyn de Kort (lordmatanza)
 * @author Michael Irwin (mikesir87)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(PullRequestReopenedEvent.class)
public class PullRequestReopenedListenerTest {

  private Notifier notifier;
  private EligibilityFilterChain filterChain;
  private PullRequestReopenedListener listener;
  private SettingsService settingsService;
  private PullRequestReopenedEvent event;
  private Repository repo;

  /**
   * Setup tasks
   */
  @Before
  public void setup() throws Exception {
    PullRequest request = mock(PullRequest.class);
    PullRequestRef toRef = mock(PullRequestRef.class);
    
    event = mock(PullRequestReopenedEvent.class);
    when(event.getPullRequest()).thenReturn(request);
    when(request.getToRef()).thenReturn(toRef);
    when(toRef.getRepository()).thenReturn(repo);
    
    notifier = mock(Notifier.class);
    filterChain = mock(EligibilityFilterChain.class);
    settingsService = mock(SettingsService.class);
    listener = new PullRequestReopenedListener(filterChain, notifier,
        settingsService);
  }

  /**
   * Validates that the notifier is used when the filter chain says ok
   */
  @Test
  public void shouldNotifyWhenChainSaysOk() throws Exception {
    Settings settings = mock(Settings.class);

    StashUser user = PowerMockito.mock(StashUser.class);
    String username = "pinky";
    ArgumentCaptor<EventContext> contextCaptor = 
        ArgumentCaptor.forClass(EventContext.class);
    when(event.getUser()).thenReturn(user);
    when(user.getName()).thenReturn(username);
    
    when(settingsService.getSettings(repo)).thenReturn(settings);
    when(filterChain.shouldDeliverNotification(contextCaptor.capture()))
        .thenReturn(true);

    listener.onPullRequestReopened(event);

    verify(notifier).notify(repo);
    assertEquals(event, contextCaptor.getValue().getEventSource());
    assertEquals(username, contextCaptor.getValue().getUsername());
    assertEquals(repo, contextCaptor.getValue().getRepository());
  }

  /**
   * Validates that the notifier is not notified when the filter chain says no
   */
  @Test
  public void shouldNotifyWhenChainSaysCancel() throws Exception {
    Repository repo = mock(Repository.class);
    Settings settings = mock(Settings.class);
    
    StashUser user = mock(StashUser.class);
    String username = "pinky";
    ArgumentCaptor<EventContext> contextCaptor = 
        ArgumentCaptor.forClass(EventContext.class);
    when(event.getUser()).thenReturn(user);
    when(user.getName()).thenReturn(username);

    when(settingsService.getSettings(repo)).thenReturn(settings);
    when(filterChain.shouldDeliverNotification(contextCaptor.capture()))
        .thenReturn(false);

    listener.onPullRequestReopened(event);

    verify(notifier, never()).notify(repo);
  }
  
  /**
   * Validates that if the repository has no settings set, execution stops
   * @throws Exception
   */
  @Test
  public void shouldntConsultChainWhenSettingsAreNull() throws Exception {
    RepositoryRefsChangedEvent e = mock(RepositoryRefsChangedEvent.class);
    Repository repo = mock(Repository.class);

    when(e.getRepository()).thenReturn(repo);
    when(settingsService.getSettings(repo)).thenReturn(null);

    listener.onPullRequestReopened(event);

    verify(notifier, never()).notify(repo);
  }
}
