package com.nerdwin15.stash.webhook.service.eligibility;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.stash.event.pull.PullRequestMergedEvent;
import com.atlassian.stash.event.pull.PullRequestRescopedEvent;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestMergeability;
import com.atlassian.stash.pull.PullRequestRef;
import com.atlassian.stash.pull.PullRequestService;
import com.atlassian.stash.repository.Repository;

/**
 * Test case for the {@link IsMergeableEligibilityFilter} class
 * 
 * @author Michael Irwin (mikesir87)
 */
public class IsMergeableEligibilityFilterTest {

  private PullRequestService pullRequestService;
  private IsMergeableEligibilityFilter filter;
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
    pullRequestService = mock(PullRequestService.class);

    repo = mock(Repository.class);
    when(repo.getId()).thenReturn(repoId);
    
    filter = new IsMergeableEligibilityFilter(pullRequestService);
    
    event = mock(PullRequestRescopedEvent.class);
    eventContext = mock(EventContext.class);
    when(eventContext.getEventSource()).thenReturn(event);
    when(eventContext.getRepository()).thenReturn(repo);
    when(eventContext.getUsername()).thenReturn(username);
  }
  
  /**
   * Validate that when the event source is not a PullRequestScopedEvent,
   * the filter just passes it on.
   */
  @Test
  public void shouldIgnoreIfEventSourceNotPullRequestScopedEvent() {
    PullRequestMergedEvent event = mock(PullRequestMergedEvent.class);
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
   * Validate that if a merge conflict would occur, the 
   */
  @Test
  public void shouldNotContinueIfMergeConflictOccurs() {
    Long prId = 2L;
    PullRequest request = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    PullRequestMergeability mergability = mock(PullRequestMergeability.class);
    
    when(event.getPullRequest()).thenReturn(request);
    when(request.getFromRef()).thenReturn(ref);
    when(request.getId()).thenReturn(prId);
    
    when(event.getPreviousFromHash()).thenReturn("event-hash");
    when(ref.getLatestChangeset()).thenReturn("ref-hash");
    when(pullRequestService.canMerge(repoId, prId)).thenReturn(mergability);
    when(mergability.isConflicted()).thenReturn(true);
    
    assertFalse(filter.shouldDeliverNotification(eventContext));
  }
  
  /**
   * Validate that if a merge conflict would not occur. 
   */
  @Test
  public void shouldContinueIfMergeConflictWillNotOccur() {
    Long prId = 2L;
    PullRequest request = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    PullRequestMergeability mergability = mock(PullRequestMergeability.class);
    
    when(event.getPullRequest()).thenReturn(request);
    when(request.getFromRef()).thenReturn(ref);
    when(request.getId()).thenReturn(prId);
    
    when(event.getPreviousFromHash()).thenReturn("event-hash");
    when(ref.getLatestChangeset()).thenReturn("ref-hash");
    when(pullRequestService.canMerge(repoId, prId)).thenReturn(mergability);
    when(mergability.isConflicted()).thenReturn(false);
    
    assertTrue(filter.shouldDeliverNotification(eventContext));
  }
  
}
