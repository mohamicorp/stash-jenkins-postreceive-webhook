package com.nerdwin15.stash.webhook.service.eligibility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.stash.event.pull.PullRequestRescopedEvent;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestService;

/**
 * An EligibilityFilter that checks if the commit that was
 * made, is auto-mergeable by Stash.
 * 
 * @author Melvyn de Kort (lordmatanza)
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
    if (!PullRequestRescopedEvent.class.isAssignableFrom(
        context.getEventSource().getClass()))
      return true;
    
    PullRequestRescopedEvent event = 
        (PullRequestRescopedEvent) context.getEventSource();
    
    PullRequest pullRequest = event.getPullRequest();
  
    if (event.getPreviousFromHash().equals(pullRequest.getFromRef()
    		.getLatestChangeset())) {
      logger.debug("Ignoring push event due to push not coming from the "
      		+ "from-side");
      return false;
    }
  
    int repoId = context.getRepository().getId();
    long pullRequestId = pullRequest.getId();

    if (pullRequestService.canMerge(repoId, pullRequestId).isConflicted()) {
      logger.debug("Ignoring push event due to conflicts in merge");
      return false;
    }

    return true;
  }

}
