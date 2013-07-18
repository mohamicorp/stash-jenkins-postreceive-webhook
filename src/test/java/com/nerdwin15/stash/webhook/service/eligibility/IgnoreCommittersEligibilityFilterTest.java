package com.nerdwin15.stash.webhook.service.eligibility;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.stash.event.RepositoryRefsChangedEvent;
import com.atlassian.stash.event.StashEvent;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.user.StashUser;
import com.nerdwin15.stash.webhook.Notifier;

/**
 * Test case for the {@link IgnoreCommittersEligibilityFilter} class
 * 
 * @author Michael Irwin (mikesir87)
 */
public class IgnoreCommittersEligibilityFilterTest {

  private RepositoryHookService hookService;
  private IgnoreCommittersEligibilityFilter filter;
  private Settings settings;
  private Repository repo;
  private RepositoryRefsChangedEvent event;
  private StashUser user;
  
  /**
   * Setup tasks
   */
  @Before
  public void setUp() throws Exception {
    hookService = mock(RepositoryHookService.class);
    repo = mock(Repository.class);
    filter = new IgnoreCommittersEligibilityFilter(hookService);
    settings = mock(Settings.class);
    when(hookService.getSettings(repo, Notifier.KEY)).thenReturn(settings);
    
    user = mock(StashUser.class);
    event = mock(RepositoryRefsChangedEvent.class);
    when(event.getRepository()).thenReturn(repo);
    when(event.getUser()).thenReturn(user);
  }
  
  /**
   * Validate that the filter should still allow delivery when no ignored
   * committers settings have been set.
   * @throws Exception
   */
  @Test
  public void shouldAllowWhenIgnoredCommittersNull() throws Exception {
    when(settings.getString(Notifier.IGNORE_COMMITTERS)).thenReturn(null);
    assertTrue(filter.shouldDeliverNotification(event));
  }
  
  /**
   * Validate that the filter should still allow delivery when the event user
   * does not match any of the ignored committers
   * @throws Exception
   */
  @Test
  public void shouldAllowWhenIgnoredCommittersDoesntMatch() throws Exception {
    when(user.getName()).thenReturn("user0");
    when(settings.getString(Notifier.IGNORE_COMMITTERS)).thenReturn("user1");
    assertTrue(filter.shouldDeliverNotification(event));
  }
  
  /**
   * Validate that the filter should cancel if an ignored committer matches
   * @throws Exception
   */
  @Test
  public void shouldCancelWhenIgnoredCommittersMatches() throws Exception {
    final String username = "user1";
    when(settings.getString(Notifier.IGNORE_COMMITTERS)).thenReturn(username);
    when(user.getName()).thenReturn(username);
    assertFalse(filter.shouldDeliverNotification(event));
  }
  
  /**
   * Validate that the filter should cancel if an ignored committer matches
   * @throws Exception
   */
  @Test
  public void shouldCancelWheMatchesWithMultipleCommitters() throws Exception {
    final String username = "user1";
    when(settings.getString(Notifier.IGNORE_COMMITTERS)).thenReturn(username 
        + " anotherUser");
    when(user.getName()).thenReturn(username);
    assertFalse(filter.shouldDeliverNotification(event));
  }
  
  /**
   * Validate that the filter should work correctly with a 
   * PullRequestMergedEvent
   * @throws Exception
   */
  @Test
  public void shouldWorkTheSameWithPullRequestMergedEvent() throws Exception {
    event = new MockedPullRequestMergedEvent();
    ((MockedPullRequestMergedEvent) event).setRepository(repo);
    ((MockedPullRequestMergedEvent) event).setUser(user);
    assertTrue(filter.shouldDeliverNotification(event));
  }
}
