package com.nerdwin15.stash.webhook.service.eligibility;

import java.util.List;

/**
 * A concrete implementation of the EligiblityFilterChain.
 * 
 * @author Michael Irwin (mikesir87)
 */
public class ConcreteEligibilityFilterChain implements EligibilityFilterChain {
  
  private List<EligibilityFilter> filters;
  
  /**
   * Construct a new instance with the provided filters
   * @param filters The EligibilityFilters to be used.
   */
  public ConcreteEligibilityFilterChain(List<EligibilityFilter> filters) {
    this.filters = filters;
  }
  
  @Override
  public boolean shouldDeliverNotification(EventContext event) {
    for (EligibilityFilter filter : filters) {
      if (!filter.shouldDeliverNotification(event))
        return false;
    }
    return true;
  }

}
