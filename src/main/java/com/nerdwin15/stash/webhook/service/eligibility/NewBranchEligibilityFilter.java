package com.nerdwin15.stash.webhook.service.eligibility;

import com.atlassian.bitbucket.event.branch.BranchCreatedEvent;
import com.atlassian.bitbucket.repository.Repository;
import com.nerdwin15.stash.webhook.service.SettingsService;

import static com.nerdwin15.stash.webhook.Notifier.OMIT_NEW_BRANCH_WITHOUT_CHANGES;

/**
 * Defines an eligibility filter that provides the ability to ignore new Branches without changes created in Bitbucket
 *
 * @author Simon Schuster (Setre14)
 */
public class NewBranchEligibilityFilter implements EligibilityFilter {

  private SettingsService settingsService;

  /**
   * Create a new instance.
   *
   * @param settingsService The settings service
   */
  public NewBranchEligibilityFilter(SettingsService settingsService) {
    this.settingsService = settingsService;
  }


  @Override
  public boolean shouldDeliverNotification(EventContext event) {
    Repository repository = event.getRepository();

    if (!settingsService.getSettings(repository).getBoolean(OMIT_NEW_BRANCH_WITHOUT_CHANGES, false)) {
      return true;
    }

    if (!BranchCreatedEvent.class.isAssignableFrom(event.getEventSource().getClass())) {
      return true;
    }

    return false;
  }
}
