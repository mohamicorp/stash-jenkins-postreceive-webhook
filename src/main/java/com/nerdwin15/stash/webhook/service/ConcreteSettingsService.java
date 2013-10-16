package com.nerdwin15.stash.webhook.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.stash.hook.repository.RepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.SecurityService;
import com.atlassian.stash.util.Operation;
import com.nerdwin15.stash.webhook.Notifier;

/**
 * Default implementation of the {@link SettingsService} interface that uses
 * a SecurityService to ensure that the current user has the ability to retrieve
 * the webhook settings.
 * 
 * @author Michael Irwin (mikesir87)
 */
public class ConcreteSettingsService implements SettingsService {

  private static final Logger LOGGER = 
      LoggerFactory.getLogger(Notifier.class);

  private RepositoryHookService hookService;
  private SecurityService securityService;
  
  /**
   * Create a new instance.
   * @param hookService The repository hook service
   * @param securityService The security service
   */
  public ConcreteSettingsService(RepositoryHookService hookService,
      SecurityService securityService) {
    this.hookService = hookService;
    this.securityService = securityService;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryHook getRepositoryHook(final Repository repository) {
    try {
      return securityService.doWithPermission("Retrieving repository hook", 
          Permission.REPO_ADMIN, new Operation<RepositoryHook, Exception>() {
        @Override
        public RepositoryHook perform() throws Exception {
          return hookService.getByKey(repository, Notifier.KEY);
        } 
      });
    } catch (Exception e) {
      LOGGER.error("Unexpected exception trying to get repository hook", e);
      return null;
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Settings getSettings(final Repository repository) {
    try {
      return securityService.doWithPermission("Retrieving settings", 
          Permission.REPO_ADMIN, new Operation<Settings, Exception>() {
        @Override
        public Settings perform() throws Exception {
          return hookService.getSettings(repository, Notifier.KEY);
        } 
      });
    } catch (Exception e) {
      LOGGER.error("Unexpected exception trying to get webhook settings", e);
      return null;
    }
  }
}
