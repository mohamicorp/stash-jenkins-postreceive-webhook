package com.nerdwin15.stash.webhook;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.RepositoryRefsChangedEvent;
import com.nerdwin15.stash.webhook.service.eligibility.EligibilityFilterChain;

/**
 * Listener for repository change events.
 * 
 * @author Michael Irwin (mikesir87)
 */
public class RepositoryChangeListener {

  private final EligibilityFilterChain filterChain;
  private final Notifier notifier;

  /**
   * Construct a new instance.
   * @param filterChain The filter chain to test for eligibility
   * @param notifier The notifier service
   */
  public RepositoryChangeListener(EligibilityFilterChain filterChain,
      Notifier notifier) {
    this.filterChain = filterChain;
    this.notifier = notifier;
  }

  /**
   * Event listener that is notified of both pull request merges and push events
   * @param event The pull request event
   */
  @EventListener
  public void onRefsChangedEvent(RepositoryRefsChangedEvent event) {
    if (filterChain.shouldDeliverNotification(event))
      notifier.notify(event.getRepository());
  }
  
}