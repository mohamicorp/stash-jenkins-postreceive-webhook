package com.nerdwin15.stash.webhook.conditions;

import java.util.Map;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.nerdwin15.stash.webhook.service.SettingsService;

/**
 * A Condition that passes when the webhook is enabled for the provided
 * repository.
 *
 * @author Michael Irwin (mikesir87)
 */
public class WebhookIsEnabledCondition implements Condition {

  private static final String REPOSITORY = "repository";
  
  private SettingsService settingsService;
  
  /**
   * Create a new instance of the condition
   * @param settingsService The settings service
   */
  public WebhookIsEnabledCondition(SettingsService settingsService) {
    this.settingsService = settingsService;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void init(Map<String, String> context) throws PluginParseException {
    // Nothing to do here
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldDisplay(Map<String, Object> context) {
    final Object obj = context.get(REPOSITORY);
    if (obj == null || !(obj instanceof Repository))
      return false;
    
    Repository repo = (Repository) obj;
    RepositoryHook hook = settingsService.getRepositoryHook(repo);
    Settings settings = settingsService.getSettings(repo);
    
    return !(hook == null || !hook.isEnabled() || settings == null);
  }
}
