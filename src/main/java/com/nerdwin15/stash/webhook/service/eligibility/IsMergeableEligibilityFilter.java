package com.nerdwin15.stash.webhook.service.eligibility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.stash.event.pull.PullRequestEvent;
import com.atlassian.stash.pull.PullRequestService;
import com.atlassian.stash.pull.PullRequestState;

/**
 * An EligibilityFilter that checks if the commit that was
 * made, is auto-mergeable by Stash.
 * 
 * This filter works with all PullRequestEvents that come through.
 * 
 * @author Melvyn de Kort (lordmatanza)
 * @author Michael Irwin (mikesir87)
 */
public class IsMergeableEligibilityFilter implements EligibilityFilter {

  private static final Logger logger = // CHECKSTYLE:logger
      LoggerFactory.getLogger(IsMergeableEligibilityFilter.class);

  private PullRequestService pullRequestService;

  /**
   * Constructs a new instance
   * @param pullRequestService Service to get the webhook settings
   */
  public IsMergeableEligibilityFilter(
      PullRequestService pullRequestService) {
    this.pullRequestService = pullRequestService;
  }

  @Override
  public boolean shouldDeliverNotification(EventContext context) {
    
    if (!PullRequestEvent.class
        .isAssignableFrom(context.getEventSource().getClass()))
      return true;
    
    PullRequestEvent event = (PullRequestEvent) context.getEventSource();
    
    if (!event.getPullRequest().getState().equals(PullRequestState.OPEN))
      return true;
    
    int repoId = context.getRepository().getId();
    long pullRequestId = event.getPullRequest().getId();

    if (pullRequestService.canMerge(repoId, pullRequestId).isConflicted()) {
      logger.debug("Ignoring push event due to conflicts in merge");
      return false;
    }

    return true;
  }

}
