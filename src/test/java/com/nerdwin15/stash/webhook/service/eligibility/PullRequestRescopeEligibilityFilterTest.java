package com.nerdwin15.stash.webhook.service.eligibility;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.stash.event.pull.PullRequestOpenedEvent;
import com.atlassian.stash.event.pull.PullRequestRescopedEvent;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestMergeability;
import com.atlassian.stash.pull.PullRequestRef;
import com.atlassian.stash.repository.Repository;

/**
 * Test case for the {@link PullRequestRescopeEligibilityFilter} class
 * 
 * @author Michael Irwin (mikesir87)
 */
public class PullRequestRescopeEligibilityFilterTest {

  private PullRequestRescopeEligibilityFilter filter;
  private Repository repo;
  private EventContext eventContext;
  private PullRequestRescopedEvent event;
  private String username = "pinky";
  private int repoId = 1;
  
  /**
   * Setup tasks
   */
  @Before
  public void setUp() throws Exception {
    repo = mock(Repository.class);
    when(repo.getId()).thenReturn(repoId);
    
    filter = new PullRequestRescopeEligibilityFilter();
    
    event = mock(PullRequestRescopedEvent.class);
    eventContext = mock(EventContext.class);
    when(eventContext.getEventSource()).thenReturn(event);
    when(eventContext.getRepository()).thenReturn(repo);
    when(eventContext.getUsername()).thenReturn(username);
  }
  
  /**
   * Validate that when the event source is not a PullRequestRescopedEvent,
   * the filter just passes it on.
   */
  @Test
  public void shouldIgnoreIfEventSourceNotPullRequestRescopedEvent() {
    PullRequestOpenedEvent event = mock(PullRequestOpenedEvent.class);
    when(eventContext.getEventSource()).thenReturn(event);
    
    assertTrue(filter.shouldDeliverNotification(eventContext));
  }

  /**
   * Validate that when the event is fired and from the "From" side, ignore it
   * and don't notify Jenkins.
   */
  @Test
  public void shouldNotContinueIfUpdateComesFromFromSide() {
    PullRequest request = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    String hash = "some-hash";
    
    when(event.getPullRequest()).thenReturn(request);
    when(request.getFromRef()).thenReturn(ref);
    
    when(event.getPreviousFromHash()).thenReturn(hash);
    when(ref.getLatestChangeset()).thenReturn(hash);
    
    assertFalse(filter.shouldDeliverNotification(eventContext));
  }
  
  /**
   * Validate that when the event is fired and not from the "From" side, 
   * notification should still continue.
   */
  @Test
  public void shouldContinueIfUpdateComesFromFromSide() {
    PullRequest request = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    String hash = "some-hash";
    
    when(event.getPullRequest()).thenReturn(request);
    when(request.getFromRef()).thenReturn(ref);
    
    when(event.getPreviousFromHash()).thenReturn(hash);
    when(ref.getLatestChangeset()).thenReturn(hash + hash);
    
    assertTrue(filter.shouldDeliverNotification(eventContext));
  }
  
}
