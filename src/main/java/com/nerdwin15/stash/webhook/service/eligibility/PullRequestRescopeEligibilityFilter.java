package com.nerdwin15.stash.webhook.service.eligibility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bitbucket.event.pull.PullRequestRescopedEvent;

/**
 * An EligibilityFilter that ensures PullRequestRescopedEvents come from
 * the "from" side.
 *
 * @author Michael Irwin (mikesir87)
 * @author Melvyn de Kort (lordmatanza)
 */
public class PullRequestRescopeEligibilityFilter implements EligibilityFilter {

  private static final Logger logger = // CHECKSTYLE:logger
      LoggerFactory.getLogger(PullRequestRescopeEligibilityFilter.class);

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldDeliverNotification(EventContext context) {
    if (!PullRequestRescopedEvent.class
        .isAssignableFrom(context.getEventSource().getClass()))
      return true;
    
    PullRequestRescopedEvent event = 
        (PullRequestRescopedEvent) context.getEventSource();
    if (event.getPreviousFromHash().equals(event.getPullRequest().getFromRef()
            .getLatestCommit())) {
      logger.debug("Ignoring push event due to push not coming from the "
              + "from-side");
      return false;
    }  
    
    return true;
  }
  
}
