package com.nerdwin15.stash.webhook;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.RepositoryRefsChangedEvent;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.nerdwin15.stash.webhook.service.eligibility.EligibilityFilterChain;

/**
 * Listener for repository change events.
 * 
 * @author Michael Irwin (mikesir87)
 */
public class RepositoryChangeListener {

  private final EligibilityFilterChain filterChain;
  private final Notifier notifier;
  private final RepositoryHookService hookService;

  /**
   * Construct a new instance.
   * @param filterChain The filter chain to test for eligibility
   * @param notifier The notifier service
   * @param hookService The hook service
   */
  public RepositoryChangeListener(EligibilityFilterChain filterChain,
      Notifier notifier, RepositoryHookService hookService) {
    this.filterChain = filterChain;
    this.notifier = notifier;
    this.hookService = hookService;
  }

  /**
   * Event listener that is notified of both pull request merges and push events
   * @param event The pull request event
   */
  @EventListener
  public void onRefsChangedEvent(RepositoryRefsChangedEvent event) {
    if (hookService.getSettings(event.getRepository(), Notifier.KEY) == null) {
      return;
    }
    if (filterChain.shouldDeliverNotification(event))
      notifier.notify(event.getRepository());
  }
  
}