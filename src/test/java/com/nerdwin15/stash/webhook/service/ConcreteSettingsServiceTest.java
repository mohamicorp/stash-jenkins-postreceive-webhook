package com.nerdwin15.stash.webhook.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.atlassian.stash.hook.repository.RepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.SecurityService;
import com.atlassian.stash.util.Operation;
import com.nerdwin15.stash.webhook.Notifier;

/**
 * Unit test for the {@link ConcreteSettingsService} test
 * 
 * @author Michael Irwin
 */
public class ConcreteSettingsServiceTest {

  private ConcreteSettingsService settingsService;
  private RepositoryHookService hookService;
  private SecurityService securityService;
  private Repository repository;
  
  /**
   * Perform setup tasks
   */
  @Before
  public void setUp() {
    hookService = mock(RepositoryHookService.class);
    securityService = mock(SecurityService.class);
    settingsService = new ConcreteSettingsService(hookService, 
        securityService);
    
    repository = mock(Repository.class);
  }
  
  /**
   * Validate the getRepositoryHook operation
   */
  @Test
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void testGetRepositoryHook() throws Throwable {
    final RepositoryHook hook = mock(RepositoryHook.class);
    ArgumentCaptor<Operation> captor = ArgumentCaptor.forClass(Operation.class);
    
    settingsService.getRepositoryHook(repository);
    verify(securityService, times(1)).doWithPermission(
        eq("Retrieving repository hook"), eq(Permission.REPO_ADMIN), 
        captor.capture());
    
    when(hookService.getByKey(repository, Notifier.KEY)).thenReturn(hook);
    Object returnValue = captor.getValue().perform();
    
    verify(hookService, times(1)).getByKey(repository, Notifier.KEY);
    assertEquals(hook, returnValue);
  }
  
  /**
   * Validate the getRepositoryHook operation
   */
  @Test
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void testGetSettings() throws Throwable {
    final Settings settings = mock(Settings.class);
    ArgumentCaptor<Operation> captor = ArgumentCaptor.forClass(Operation.class);
    
    settingsService.getSettings(repository);
    verify(securityService, times(1)).doWithPermission(
        eq("Retrieving settings"), eq(Permission.REPO_ADMIN), captor.capture());
    
    when(hookService.getSettings(repository, Notifier.KEY))
        .thenReturn(settings);
    Object returnValue = captor.getValue().perform();
    
    verify(hookService, times(1)).getSettings(repository, Notifier.KEY);
    assertEquals(settings, returnValue);
  }
}
