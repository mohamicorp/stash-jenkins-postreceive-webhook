package com.nerdwin15.stash.webhook.service.eligibility;

import com.atlassian.stash.repository.Repository;

/**
 * A context object that will be used throughout the eligibility filter chain.
 * The purpose is to encapsulate the original event and provide a single
 * context object.
 *
 * @author Michael Irwin (mikesir87)
 */
public class EventContext {

  private final Object eventSource;
  private final Repository repository;
  private final String username;
  
  /**
   * Constructs a new context instance
   * @param source The original event source
   * @param repository The repository being affected by the event
   * @param username The username of the user that initiated the event
   */
  public EventContext(Object source, Repository repository, String username) {
    this.eventSource = source;
    this.repository = repository;
    this.username = username;
  }
  
  /**
   * Gets the {@code eventSource} property.
   * @return The original source for the event
   */
  public Object getEventSource() {
    return eventSource;
  }
  /**
   * Gets the {@code repository} property.
   * @return The repository that was affected by this event
   */
  public Repository getRepository() {
    return repository;
  }
  
  /**
   * Gets the {@code username} property.
   * @return The username of the user that initiated the event
   */
  public String getUsername() {
    return username;
  }
}
