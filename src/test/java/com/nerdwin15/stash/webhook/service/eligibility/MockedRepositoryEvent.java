package com.nerdwin15.stash.webhook.service.eligibility;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.stash.event.RepositoryRefsChangedEvent;
import com.atlassian.stash.repository.RefChange;
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
public class MockedRepositoryEvent implements RepositoryRefsChangedEvent {

  private static final long serialVersionUID = 6015228907835452814L;

  private Repository repository;
  private StashUser user;
  
  /**
   * Creates a new instance
   * @param repository The repository
   */
  public MockedRepositoryEvent(Repository repository) {
    this.repository = repository;
  }

  /**
   * Set the StashUser for the event, using reflection.
   * @param user The user for the event
   */
  public void setUser(StashUser user) throws Exception {
    this.user = user;
  }

  @Override
  @Nonnull
  public Collection<RefChange> getRefChanges() {
    return new ArrayList<RefChange>();
  }

  @Override
  @Nonnull
  public Repository getRepository() {
    return repository;
  }

  @Override
  @Nullable
  public StashUser getUser() {
    return user;
  }

}
