package com.nerdwin15.stash.webhook.conditions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.nerdwin15.stash.webhook.service.SettingsService;

/**
 * Test for the WebhookIsEnabledCondition class
 * 
 * @author Michael Irwin (mikesir87)
 */
public class WebhookIsEnabledConditionTest {

  private Repository repo;
  private RepositoryHook repoHook;
  private Settings settings;
  private SettingsService settingsService;
  private Map<String, Object> context;
  
  private WebhookIsEnabledCondition condition;

  /**
   * Setup tasks
   */
  @Before
  public void setup() throws Exception {
    settingsService = mock(SettingsService.class);
    repo = mock(Repository.class);
    repoHook = mock(RepositoryHook.class);
    settings = mock(Settings.class);

    context = new HashMap<String, Object>();
    context.put("repository", repo);

    when(repoHook.isEnabled()).thenReturn(true);
    when(settingsService.getRepositoryHook(repo)).thenReturn(repoHook);
    when(settingsService.getSettings(repo)).thenReturn(settings);
    
    condition = new WebhookIsEnabledCondition(settingsService);
  }

  /**
   * Ensure that if the repository is not found in the context, display should
   * fail
   */
  @Test
  public void testShouldNotDisplayIfRepositoryNullOrNotRepository() {
    context.put("repository", null);
    assertFalse(condition.shouldDisplay(context));
    
    context.put("repository", "notARepository");
    assertFalse(condition.shouldDisplay(context));
  }
  
  /**
   * Ensure that if the hook is null, shouldDisplay returns false
   */
  @Test
  public void testShouldNotDisplayIfHookIsNull() {
    when(settingsService.getRepositoryHook(repo)).thenReturn(null);
    assertFalse(condition.shouldDisplay(context));
  }
  
  /**
   * Ensure that if the hook is disabled, shouldDisplay returns false
   */
  @Test
  public void testShouldNotDisplayIfHookIsDisabled() {
    when(repoHook.isEnabled()).thenReturn(false);
    assertFalse(condition.shouldDisplay(context));
  }
  
  /**
   * Ensure that if the settings are null, shouldDisplay returns false
   */
  @Test
  public void testShouldNotDisplayIfSettingsIsNull() {
    when(settingsService.getSettings(repo)).thenReturn(null);
    assertFalse(condition.shouldDisplay(context));
  }

  /**
   * Ensure that when everything is set correctly, shouldDisplay is true
   */
  @Test
  public void testShouldDisplayWhenEverythingIsSetRight() {
    assertTrue(condition.shouldDisplay(context));
  }
  
}
