package com.nerdwin15.stash.webhook;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.stash.event.pull.PullRequestMergedEvent;
import com.atlassian.stash.repository.Repository;
import com.nerdwin15.stash.webhook.service.eligibility.EligibilityFilterChain;

/**
 * Test case for the MergeListener class.
 * 
 * @author Peter Leibiger (kuhnroyal)
 * @author Michael Irwin (mikesir87)
 */
public class MergeListenerTest {

  private Notifier notifier;
  private EligibilityFilterChain filterChain;
  private MergeListener listener;

  /**
   * Setup tasks
   */
  @Before
  public void setup() throws Exception {
    notifier = mock(Notifier.class);
    filterChain = mock(EligibilityFilterChain.class);
    listener = new MergeListener(filterChain, notifier);
  }

  /**
   * Validates that the notifier is used when the filter chain says ok
   */
  @Test
  public void shouldNotifyWhenChainSaysOk() throws Exception {
    PullRequestMergedEvent e = mock(PullRequestMergedEvent.class);
    Repository repo = mock(Repository.class);

    when(e.getRepository()).thenReturn(repo);
    when(filterChain.shouldDeliverNotification(e)).thenReturn(true);

    listener.onPullRequestMerged(e);

    verify(notifier).notify(repo);
  }

  /**
   * Validates that the notifier is not notified when the filter chain says no
   */
  @Test
  public void shouldNotifyWhenChainSaysCancel() throws Exception {
    PullRequestMergedEvent e = mock(PullRequestMergedEvent.class);
    Repository repo = mock(Repository.class);

    when(e.getRepository()).thenReturn(repo);
    when(filterChain.shouldDeliverNotification(e)).thenReturn(false);

    listener.onPullRequestMerged(e);

    verify(notifier, never()).notify(repo);
  }
}
