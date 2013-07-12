package com.nerdwin15.stash.webhook.service.eligibility;


/**
 * An EligibilityFilterChain holds several EligibilityFilters and uses them to
 * determine if a notification should be delivered based on a StashEvent.
 *  
 * @author Michael Irwin (mikesir87)
 */
public interface EligibilityFilterChain extends EligibilityFilter {

}
