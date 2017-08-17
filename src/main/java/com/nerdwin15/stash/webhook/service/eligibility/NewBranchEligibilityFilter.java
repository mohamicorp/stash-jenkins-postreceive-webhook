package com.nerdwin15.stash.webhook.service.eligibility;

import com.atlassian.bitbucket.event.branch.BranchCreatedEvent;
import com.atlassian.bitbucket.event.repository.RepositoryRefsChangedEvent;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.Repository;
import com.nerdwin15.stash.webhook.service.BranchEvaluator;
import com.nerdwin15.stash.webhook.service.SettingsService;

import java.util.Iterator;

import static com.nerdwin15.stash.webhook.Notifier.OMIT_NEW_BRANCH_WITHOUT_CHANGES;

public class NewBranchEligibilityFilter implements EligibilityFilter {

  private SettingsService settingsService;
  private BranchEvaluator branchEvaluator;

  /**
   * Create a new instance.
   *
   * @param settingsService The settings service
   * @param branchEvaluator An evaluator to determine affected branches.
   */
  public NewBranchEligibilityFilter(SettingsService settingsService,
                                    BranchEvaluator branchEvaluator) {
    this.settingsService = settingsService;
    this.branchEvaluator = branchEvaluator;
  }


  @Override
  public boolean shouldDeliverNotification(EventContext event) {
    Repository repository = event.getRepository();

    //!OmitNewbranchesWithoutChanges => true
    if (!settingsService.getSettings(repository).getBoolean(OMIT_NEW_BRANCH_WITHOUT_CHANGES, false)) {
      return true;
    }

//    EventSource e = (EventSource) event.getEventSource();

    //!NewBranch => true


    //New branch created in BitBucket => false
    if (!BranchCreatedEvent.class.isAssignableFrom(event.getEventSource().getClass())) {
      return true;
    }

    return false;
  }
}
