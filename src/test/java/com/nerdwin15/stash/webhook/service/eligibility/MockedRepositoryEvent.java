package com.nerdwin15.stash.webhook.service.eligibility;

import java.lang.reflect.Field;

import com.atlassian.stash.event.RepositoryEvent;
import com.atlassian.stash.event.StashEvent;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.user.StashUser;

/**
 * A RepositoryEvent used for testing.
 * 
 * Had to mock because the getUser is final and cannot be stubbed. This sets the
 * user field through reflection, allowing the getUser to still work.
 * 
 * @author Michael Irwin (mikesir87)
 */
public class MockedRepositoryEvent extends RepositoryEvent {

  private static final long serialVersionUID = 6015228907835452814L;

  /**
   * Creates a new instance
   * @param repository The repository
   */
  public MockedRepositoryEvent(Repository repository) {
    super("TEST", repository);
  }

  /**
   * Set the StashUser for the event, using reflection.
   * @param user The user for the event
   */
  public void setUser(StashUser user) throws Exception {
    Field field = StashEvent.class.getDeclaredField("user");
    field.setAccessible(true);
    field.set(this, user);
  }

}
