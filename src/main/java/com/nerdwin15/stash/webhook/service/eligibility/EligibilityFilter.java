package com.nerdwin15.stash.webhook.service.eligibility;

import com.atlassian.stash.event.RepositoryRefsChangedEvent;

/**
 * A SendingFilter represents a single filter in an EligibilityFilterChain that
 * determines whether a notification is eligible for deliver of a notification.
 *  
 * A particular filter should only say it cannot be delivered if it can 
 * authoritatively state that it should not be delivered.
 *  
 * @author Michael Irwin (mikesir87)
 */
public interface EligibilityFilter {

  /**
   * Determines whether a notification should be delivered based on the provided
   * event.
   * @param event The event to be analyzed.
   * @return True if the notification should be delivered or if the particular
   * filter cannot assertively determine that it should not be delivered.
   */
  boolean shouldDeliverNotification(RepositoryRefsChangedEvent event);
  
}
