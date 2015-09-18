package com.nerdwin15.stash.webhook;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.atlassian.bitbucket.repository.RefChange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.atlassian.bitbucket.event.repository.RepositoryRefsChangedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestEvent;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.nerdwin15.stash.webhook.service.SettingsService;
import com.nerdwin15.stash.webhook.service.eligibility.EligibilityFilterChain;
import com.nerdwin15.stash.webhook.service.eligibility.EventContext;

/**
 * Test case for the PullRequestRescopeListener class.
 * 
 * @author Michael Irwin (mikesir87)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(PullRequestEvent.class)
public class PullRequestEventListenerTest {

  private Notifier notifier;
  private EligibilityFilterChain filterChain;
  private PullRequestEventListener listener;
  private SettingsService settingsService;
  private PullRequestEvent event;
  private Repository repo;

  /**
   * Setup tasks
   */
  @Before
  public void setup() throws Exception {
    PullRequest request = mock(PullRequest.class);
    PullRequestRef toRef = mock(PullRequestRef.class);
    
    event = mock(PullRequestEvent.class);
    when(event.getPullRequest()).thenReturn(request);
    when(request.getToRef()).thenReturn(toRef);
    when(toRef.getRepository()).thenReturn(repo);
    
    notifier = mock(Notifier.class);
    filterChain = mock(EligibilityFilterChain.class);
    settingsService = mock(SettingsService.class);
    listener = new PullRequestEventListener(filterChain, notifier, 
        settingsService);
  }

  /**
   * Validates that the notifier is used when the filter chain says ok
   */
//  @Test
//  public void shouldNotifyWhenChainSaysOk() throws Exception {
//    PullRequestRef r = Mockito.mock(PullRequestRef.class);
//    Mockito.when(r.toString()).thenReturn("project/repo:refs/heads/master");
//    Mockito.when(r.getLatestChangeset()).thenReturn("sha1");
//    when(event.getPullRequest().getFromRef()).thenReturn(r);
//
//    Settings settings = mock(Settings.class);
//
//    StashUser user = PowerMockito.mock(StashUser.class);
//    String username = "pinky";
//    ArgumentCaptor<EventContext> contextCaptor =
//        ArgumentCaptor.forClass(EventContext.class);
//    when(event.getUser()).thenReturn(user);
//    when(user.getName()).thenReturn(username);
//
//    when(settingsService.getSettings(repo)).thenReturn(settings);
//    when(filterChain.shouldDeliverNotification(contextCaptor.capture()))
//        .thenReturn(true);
//
//    listener.handleEvent(event);
//
//    verify(notifier).notifyBackground(repo, "master", "sha1");
//    assertEquals(event, contextCaptor.getValue().getEventSource());
//    assertEquals(username, contextCaptor.getValue().getUsername());
//    assertEquals(repo, contextCaptor.getValue().getRepository());
//  }

  /**
   * Validates that the notifier is not notified when the filter chain says no
   */
//  @Test
//  public void shouldNotifyWhenChainSaysCancel() throws Exception {
//    Repository repo = mock(Repository.class);
//    Settings settings = mock(Settings.class);
//
//    StashUser user = mock(StashUser.class);
//    String username = "pinky";
//    ArgumentCaptor<EventContext> contextCaptor =
//        ArgumentCaptor.forClass(EventContext.class);
//    when(event.getUser()).thenReturn(user);
//    when(user.getName()).thenReturn(username);
//
//    when(settingsService.getSettings(repo)).thenReturn(settings);
//    when(filterChain.shouldDeliverNotification(contextCaptor.capture()))
//        .thenReturn(false);
//
//    listener.handleEvent(event);
//
//    verify(notifier, never()).notifyBackground(repo, "master", "sha1");
//  }
  
  /**
   * Validates that if the repository has no settings set, execution stops
   * @throws Exception
   */
//  @Test
//  public void shouldntConsultChainWhenSettingsAreNull() throws Exception {
//    RepositoryRefsChangedEvent e = mock(RepositoryRefsChangedEvent.class);
//    Repository repo = mock(Repository.class);
//
//    when(e.getRepository()).thenReturn(repo);
//    when(settingsService.getSettings(repo)).thenReturn(null);
//
//    listener.handleEvent(event);
//
//    verify(notifier, never()).notifyBackground(repo, "master", "sha1");
//  }
}
