package com.nerdwin15.stash.webhook;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.RepositoryPushEvent;
import com.atlassian.stash.hook.repository.AsyncPostReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.RepositorySettingsValidator;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;
import com.google.common.base.Strings;
import com.nerdwin15.stash.webhook.service.eligibility.EligibilityFilterChain;

/**
 * Note that hooks can implement RepositorySettingsValidator directly.
 */
public class PostReceiveHook implements AsyncPostReceiveRepositoryHook, 
    RepositorySettingsValidator {

  private final EligibilityFilterChain eligibilityFilter;
  private final Notifier notifier;

  /**
   * Constructs a new instance.
   * @param eligibilityFilter An EligibilityFilterChain
   * @param notifier The notify service to use for notification
   */
  public PostReceiveHook(EligibilityFilterChain eligibilityFilter,
      Notifier notifier) {
    
    this.eligibilityFilter = eligibilityFilter;
    this.notifier = notifier;
  }
  
  /**
   * Fire off the notifier of the push event that has occurred
   * @param event The repository push event
   */
  @EventListener
  public void onPushEvent(RepositoryPushEvent event) {
    if (eligibilityFilter.shouldDeliverNotification(event))
      notifier.notify(event.getRepository());
  }
  
  @Override
  public void postReceive(@Nonnull RepositoryHookContext ctx, 
      @Nonnull Collection<RefChange> changes) {
    // Don't do anything since the event is being handled in the onPushEvent
  }

  @Override
  public void validate(@Nonnull Settings settings, 
      @Nonnull SettingsValidationErrors errors, 
      @Nonnull Repository repository) {
    
    final String jenkinsUrl = settings.getString(Notifier.JENKINS_BASE);
    if (Strings.isNullOrEmpty(jenkinsUrl)) {
      errors.addFieldError(Notifier.JENKINS_BASE, 
          "The url for your Jenkins instance is required.");
    }
    
    final String cloneType = settings.getString(Notifier.CLONE_TYPE);
    if (Strings.isNullOrEmpty(cloneType)) {
      errors.addFieldError(Notifier.CLONE_TYPE, 
          "The repository clone type is required");
    }
    else if (!cloneType.equals("http") && !cloneType.equals("ssh")) {
      errors.addFieldError(Notifier.CLONE_TYPE, 
          "A valid clone type is required.");
    }
  }
}
