package com.nerdwin15.stash.webhook.service;

import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;

/**
 * Utility class used to retrieve the Settings for a given repository
 * 
 * @author Michael Irwin (mikesir87)
 */
public interface SettingsService {

  /**
   * Get the repository hook context for this hook in the provided repository.
   * @param repository The repository
   * @return The hook context
   */
  RepositoryHook getRepositoryHook(Repository repository);
  
  /**
   * Get the webhook settings for the provided repository.
   * @param repository The Repository
   * @return The webhook settings for the repository. Null if no settings set.
   */
  Settings getSettings(Repository repository);
}
