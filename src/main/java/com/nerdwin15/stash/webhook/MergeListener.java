package com.nerdwin15.stash.webhook;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.PullRequestMergedEvent;
import com.nerdwin15.stash.webhook.service.eligibility.EligibilityFilterChain;

/**
 * Listener for pull request merges, which then sends out notifications.
 * 
 * @author Peter Leibiger (kuhnroyal)
 * @author Michael Irwin (mikesir87)
 */
public class MergeListener {

  private final EligibilityFilterChain filterChain;
  private final Notifier notifier;

  /**
   * Construct a new instance.
   * @param filterChain The filter chain to test for eligbility
   * @param notifier The notifier service
   */
  public MergeListener(EligibilityFilterChain filterChain,
      Notifier notifier) {
    this.filterChain = filterChain;
    this.notifier = notifier;
  }

  /**
   * Event listener that is notified of pull request merges.
   * @param event The pull request event
   */
  @EventListener
  public void onPullRequestMerged(PullRequestMergedEvent event) {
    if (filterChain.shouldDeliverNotification(event))
      notifier.notify(event.getRepository());
  }
  
}