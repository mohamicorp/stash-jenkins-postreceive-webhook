package com.nerdwin15.stash.webhook;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.PullRequestEvent;
import com.atlassian.stash.event.pull.PullRequestOpenedEvent;
import com.atlassian.stash.event.pull.PullRequestReopenedEvent;
import com.atlassian.stash.event.pull.PullRequestRescopedEvent;
import com.nerdwin15.stash.webhook.service.SettingsService;
import com.nerdwin15.stash.webhook.service.eligibility.EligibilityFilterChain;
import com.nerdwin15.stash.webhook.service.eligibility.EventContext;

/**
 * Event listener that listens to PullRequestRescopedEvent events.
 * 
 * @author Michael Irwin (mikesir87)
 * @author Melvyn de Kort (lordmatanza)
 */
public class PullRequestEventListener {
  
  private final EligibilityFilterChain filterChain;
  private final Notifier notifier;
  private final SettingsService settingsService;

  /**
   * Construct a new instance.
   * @param filterChain The filter chain to test for eligibility
   * @param notifier The notifier service
   * @param settingsService Service to be used to get the Settings
   */
  public PullRequestEventListener(EligibilityFilterChain filterChain,
      Notifier notifier, SettingsService settingsService) {
    this.filterChain = filterChain;
    this.notifier = notifier;
    this.settingsService = settingsService;
  }
  
  /**
   * Event listener that is notified of pull request open events
   * @param event The pull request event
   */
  @EventListener
  public void onPullRequestOpened(PullRequestOpenedEvent event) {
    handleEvent(event);
  }
  
  /**
   * Event listener that is notified of pull request reopen events
   * @param event The pull request event
   */
  @EventListener
  public void onPullRequestReopened(PullRequestReopenedEvent event) {
    handleEvent(event);
  }
  
  /**
   * Actually handles the event that was triggered. 
   * (Made protected to make unit testing easier)
   * @param event The event to be handled
   */
  protected void handleEvent(PullRequestEvent event) {
    if (settingsService.getSettings(event.getPullRequest().getToRef()
        .getRepository()) == null) {
      return;
    }

    String strRef = event.getPullRequest().getFromRef().toString()
        .replaceFirst(".*refs/heads/", "");
    String strSha1 = event.getPullRequest().getFromRef().getLatestChangeset();

    EventContext context = new EventContext(event,
        event.getPullRequest().getToRef().getRepository(),
        event.getUser().getName());

    if (filterChain.shouldDeliverNotification(context))
      notifier.notifyBackground(context.getRepository(), strRef, strSha1);
  }
  
}
