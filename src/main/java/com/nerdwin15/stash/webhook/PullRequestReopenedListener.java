package com.nerdwin15.stash.webhook;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.PullRequestOpenedEvent;
import com.atlassian.stash.event.pull.PullRequestReopenedEvent;
import com.nerdwin15.stash.webhook.service.SettingsService;
import com.nerdwin15.stash.webhook.service.eligibility.EligibilityFilterChain;
import com.nerdwin15.stash.webhook.service.eligibility.EventContext;

/**
 * Event listener that listens to PullRequestReopenedEvent events.
 *
 * @author Melvyn de Kort (lordmatanza)
 * @author Michael Irwin (mikesir87)
 */
public class PullRequestReopenedListener {

  private final EligibilityFilterChain filterChain;
  private final Notifier notifier;
  private final SettingsService settingsService;

  /**
   * Construct a new instance.
   * @param filterChain The filter chain to test for eligibility
   * @param notifier The notifier service
   * @param settingsService Service to be used to get the Settings
   */
  public PullRequestReopenedListener(EligibilityFilterChain filterChain,
                                     Notifier notifier, SettingsService settingsService) {
    this.filterChain = filterChain;
    this.notifier = notifier;
    this.settingsService = settingsService;
  }
  
  /**
   * Event listener that is notified of pull request reopened events
   * @param event The pull request event
   */
  @EventListener
  public void onPullRequestReopened(PullRequestReopenedEvent event) {
    if (settingsService.getSettings(event.getPullRequest().getToRef()
        .getRepository()) == null) {
      return;
    }
    
    EventContext context = new EventContext(event, 
        event.getPullRequest().getToRef().getRepository(), 
        event.getUser().getName());
    
    if (filterChain.shouldDeliverNotification(context))
      notifier.notify(context.getRepository());
  }
  
}
