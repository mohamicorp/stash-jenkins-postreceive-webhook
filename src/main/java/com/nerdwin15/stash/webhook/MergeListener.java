package com.nerdwin15.stash.webhook;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.PullRequestMergedEvent;

public class MergeListener {

    private final Notifier notifier;

    public MergeListener(Notifier notifier) {
        this.notifier = notifier;
    }

    @EventListener
    public void onPullRequestMerged(PullRequestMergedEvent e) {
        notifier.notify(e.getRepository());
    }
}