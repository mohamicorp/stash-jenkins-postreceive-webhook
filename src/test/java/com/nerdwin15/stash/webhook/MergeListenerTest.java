package com.nerdwin15.stash.webhook;

import com.atlassian.stash.event.pull.PullRequestMergedEvent;
import com.atlassian.stash.repository.Repository;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class MergeListenerTest {

    private Notifier notifier;

    private MergeListener listener;

    @Before
    public void setup() throws Exception {
        notifier = mock(Notifier.class);
        listener = new MergeListener(notifier);
    }

    @Test
    public void testNotify() throws Exception {
        PullRequestMergedEvent e = mock(PullRequestMergedEvent.class);
        Repository repo = mock(Repository.class);

        when(e.getRepository()).thenReturn(repo);

        listener.onPullRequestMerged(e);

        verify(notifier).notify(repo);
    }
}
