package com.nerdwin15.stash.webhook;

import com.atlassian.stash.event.pull.PullRequestMergedEvent;
import com.atlassian.stash.repository.Repository;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Test case for the MergeListener class.
 * 
 * @author Peter Leibiger (kuhnroyal)
 * @author Michael Irwin (mikesir87)
 */
public class MergeListenerTest {

  private Notifier notifier;
  private MergeListener listener;

  /**
   * Setup tasks
   */
  @Before
  public void setup() throws Exception {
    notifier = mock(Notifier.class);
    listener = new MergeListener(notifier);
  }

  /**
   * Validates that the notifier is used to send the notification.
   */
  @Test
  public void testNotify() throws Exception {
    PullRequestMergedEvent e = mock(PullRequestMergedEvent.class);
    Repository repo = mock(Repository.class);

    when(e.getRepository()).thenReturn(repo);

    listener.onPullRequestMerged(e);

    verify(notifier).notify(repo);
  }
}
