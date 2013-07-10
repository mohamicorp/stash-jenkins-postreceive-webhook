package com.nerdwin15.stash.webhook;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.PullRequestMergedEvent;

/**
 * Listener for pull request merges, which then sends out notifications.
 * 
 * @author Peter Leibiger (kuhnroyal)
 * @author Michael Irwin (mikesir87)
 */
public class MergeListener {

  private final Notifier notifier;

  /**
   * Construct a new instance.
   * @param notifier The notifier service
   */
  public MergeListener(Notifier notifier) {
    this.notifier = notifier;
  }

  /**
   * Event listener that is notified of pull request merges.
   * @param e The pull request event
   */
  @EventListener
  public void onPullRequestMerged(PullRequestMergedEvent e) {
    notifier.notify(e.getRepository());
  }
  
}