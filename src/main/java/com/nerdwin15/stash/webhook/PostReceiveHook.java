package com.nerdwin15.stash.webhook;

import com.atlassian.stash.hook.repository.AsyncPostReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.RepositorySettingsValidator;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;
import com.google.common.base.Strings;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Note that hooks can implement RepositorySettingsValidator directly.
 */
public class PostReceiveHook implements AsyncPostReceiveRepositoryHook, 
    RepositorySettingsValidator {

  private final Notifier notifier;

  /**
   * Constructs a new instance.
   * @param notifier The notify service to use for notification
   */
  public PostReceiveHook(Notifier notifier) {
    this.notifier = notifier;
  }

  @Override
  public void postReceive(@Nonnull RepositoryHookContext ctx, 
      @Nonnull Collection<RefChange> changes) {
    notifier.notify(ctx.getRepository());
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
  }
}
