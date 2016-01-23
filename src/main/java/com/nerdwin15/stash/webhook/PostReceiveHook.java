package com.nerdwin15.stash.webhook;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.atlassian.bitbucket.hook.repository.AsyncPostReceiveRepositoryHook;
import com.atlassian.bitbucket.hook.repository.RepositoryHookContext;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.RepositorySettingsValidator;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsValidationErrors;
import com.google.common.base.Strings;

/**
 * Note that hooks can implement RepositorySettingsValidator directly.
 */
public class PostReceiveHook implements AsyncPostReceiveRepositoryHook, 
    RepositorySettingsValidator {
  
  @Override
  public void postReceive(@Nonnull RepositoryHookContext ctx, 
      @Nonnull Collection<RefChange> changes) {
    // Don't do anything since the event will be handled by the 
    // RepositoryChangeListener
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
    } else if (!cloneType.equals("http") && !cloneType.equals("ssh") && !cloneType.equals("custom")) {
        errors.addFieldError(Notifier.CLONE_TYPE,
                "The repository clone type is invalid");
    }
    
    final String cloneUrl = settings.getString(Notifier.CLONE_URL);
    if (cloneType == null || cloneType.equals("custom")) {
        if (Strings.isNullOrEmpty(cloneUrl)) {
          errors.addFieldError(Notifier.CLONE_URL,
              "The repository clone url is required");
        }
    }
    
    final String branchSelection = settings.getString(Notifier.BRANCH_OPTIONS);
    if (!Strings.isNullOrEmpty(branchSelection)) {
      String branches = settings.getString(Notifier.BRANCH_OPTIONS_BRANCHES);
      if (Strings.isNullOrEmpty(branches)) {
        errors.addFieldError(Notifier.BRANCH_OPTIONS_BRANCHES, 
            "No branches were specified to " + branchSelection);
      }
    }
  }
}
