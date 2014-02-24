package com.nerdwin15.stash.webhook;


/**
 * A model object that wraps the result from a notification attempt.
 *
 * @author Michael Irwin (mikesir87)
 */
public class NotificationResult {

  private final boolean successful;
  private final String url;
  private final String message;
  
  /**
   * Create a new result
   * @param successful Was the notification successful?
   * @param url The URL that was used for notification
   * @param message Either an error message or the body of the response from
   * Jenkins
   */
  public NotificationResult(boolean successful, String url, String message) {
    this.successful = successful;
    this.url = url;
    this.message = message;
  }
  
  /**
   * Gets the {@code successful} property.
   * @return Was the notification successful?
   */
  public boolean isSuccessful() {
    return successful;
  }
  
  /**
   * Gets the {@code message} property.
   * @return Either an error message or the response from the server.
   */
  public String getMessage() {
    return message;
  }
  
  /**
   * Gets the {@code url} property.
   * @return The URL used to notify Jenkins.
   */
  public String getUrl() {
    return url;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "Result: successful? " + successful + "; url: " + url 
        + "; message: " + message;
  }
  
}
