package com.nerdwin15.stash.webhook;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.RepositoryRefsChangedEvent;
import com.atlassian.stash.event.pull.PullRequestRescopedEvent;
import com.nerdwin15.stash.webhook.service.SettingsService;
import com.nerdwin15.stash.webhook.service.eligibility.EligibilityFilterChain;

/**
 * Listener for repository change events.
 * 
 * @author Michael Irwin (mikesir87)
 */
public class RepositoryChangeListener {

  private final EligibilityFilterChain filterChain;
  private final Notifier notifier;
  private final SettingsService settingsService;

  /**
   * Construct a new instance.
   * @param filterChain The filter chain to test for eligibility
   * @param notifier The notifier service
   * @param settingsService Service to be used to get the Settings
   */
  public RepositoryChangeListener(EligibilityFilterChain filterChain,
      Notifier notifier, SettingsService settingsService) {
    this.filterChain = filterChain;
    this.notifier = notifier;
    this.settingsService = settingsService;
  }

  /**
   * Event listener that is notified of both pull request merges and push events
   * @param event The pull request event
   */
  @EventListener
  public void onRefsChangedEvent(RepositoryRefsChangedEvent event) {
    if (settingsService.getSettings(event.getRepository()) == null) {
      return;
    }
    if (filterChain.shouldDeliverNotification(event))
      notifier.notify(event.getRepository());
  }

  /**
   * Event listener that is notified of pull request rescope events
   * @param event The pull request event
   */
  @EventListener
  public void onPullRequestRescope(PullRequestRescopedEvent event) {
    if (settingsService.getSettings(event.getPullRequest().getToRef()
    		.getRepository()) == null) {
      return;
    }
    if (filterChain.shouldDeliverNotification(event))
      notifier.notify(event.getPullRequest().getToRef().getRepository()); 
  }

}
