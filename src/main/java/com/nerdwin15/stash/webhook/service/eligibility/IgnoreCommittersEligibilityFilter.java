package com.nerdwin15.stash.webhook.service.eligibility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.stash.event.RepositoryEvent;
import com.atlassian.stash.event.StashEvent;
import com.atlassian.stash.event.pull.PullRequestMergedEvent;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.nerdwin15.stash.webhook.Notifier;

/**
 * An EligibilityFilter that checks if the user that initiated the StashEvent is
 * a user that is in the ignores list for the hook configuration.
 * 
 * @author Michael Irwin (mikesir87)
 */
public class IgnoreCommittersEligibilityFilter implements EligibilityFilter {

  private static final Logger logger = // CHECKSTYLE:logger
  LoggerFactory.getLogger(IgnoreCommittersEligibilityFilter.class);

  private RepositoryHookService hookService;

  /**
   * Constructs a new instance
   * 
   * @param hookService
   *          The hook service
   */
  public IgnoreCommittersEligibilityFilter(RepositoryHookService hookService) {
    this.hookService = hookService;
  }

  @Override
  public boolean shouldDeliverNotification(StashEvent e) {
    if (RepositoryEvent.class.isAssignableFrom(e.getClass()))
      return doCheck(e, ((RepositoryEvent) e).getRepository());
    if (PullRequestMergedEvent.class.isAssignableFrom(e.getClass()))
      return doCheck(e, ((PullRequestMergedEvent) e).getRepository());
    return true;
  }
  
  private boolean doCheck(StashEvent event, Repository repository) {
    String eventUserName = event.getUser().getName();

    final Settings settings = hookService.getSettings(repository,
        Notifier.KEY);
    String ignoreCommitters = settings.getString(Notifier.IGNORE_COMMITTERS);
    if (ignoreCommitters == null)
      return true;

    for (String committer : ignoreCommitters.split(" ")) {
      if (committer.equalsIgnoreCase(eventUserName)) {
        logger.debug("Ignoring push event due to ignore committer {}",
            committer);
        return false;
      }
    }
    return true;
  }
}
