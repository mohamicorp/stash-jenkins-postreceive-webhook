package com.nerdwin15.stash.webhook;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.stash.event.RepositoryRefsChangedEvent;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.nerdwin15.stash.webhook.service.SettingsService;
import com.nerdwin15.stash.webhook.service.eligibility.EligibilityFilterChain;

/**
 * Test case for the RepositoryChangeListener class.
 * 
 * @author Michael Irwin (mikesir87)
 */
public class RepositoryChangeListenerTest {

  private Notifier notifier;
  private EligibilityFilterChain filterChain;
  private RepositoryChangeListener listener;
  private SettingsService settingsService;

  /**
   * Setup tasks
   */
  @Before
  public void setup() throws Exception {
    notifier = mock(Notifier.class);
    filterChain = mock(EligibilityFilterChain.class);
    settingsService = mock(SettingsService.class);
    listener = new RepositoryChangeListener(filterChain, notifier, 
        settingsService);
  }

  /**
   * Validates that the notifier is used when the filter chain says ok
   */
  @Test
  public void shouldNotifyWhenChainSaysOk() throws Exception {
    RepositoryRefsChangedEvent e = mock(RepositoryRefsChangedEvent.class);
    Repository repo = mock(Repository.class);
    Settings settings = mock(Settings.class);

    when(e.getRepository()).thenReturn(repo);
    when(settingsService.getSettings(repo)).thenReturn(settings);
    when(filterChain.shouldDeliverNotification(e)).thenReturn(true);

    listener.onRefsChangedEvent(e);

    verify(notifier).notify(repo);
  }

  /**
   * Validates that the notifier is not notified when the filter chain says no
   */
  @Test
  public void shouldNotifyWhenChainSaysCancel() throws Exception {
    RepositoryRefsChangedEvent e = mock(RepositoryRefsChangedEvent.class);
    Repository repo = mock(Repository.class);
    Settings settings = mock(Settings.class);

    when(e.getRepository()).thenReturn(repo);
    when(settingsService.getSettings(repo)).thenReturn(settings);
    when(filterChain.shouldDeliverNotification(e)).thenReturn(false);

    listener.onRefsChangedEvent(e);

    verify(notifier, never()).notify(repo);
  }
  
  /**
   * Validates that if the repository has no settings set, execution stops
   * @throws Exception
   */
  @Test
  public void shouldntConsultChainWhenSettingsAreNull() throws Exception {
    RepositoryRefsChangedEvent e = mock(RepositoryRefsChangedEvent.class);
    Repository repo = mock(Repository.class);

    when(e.getRepository()).thenReturn(repo);
    when(settingsService.getSettings(repo)).thenReturn(null);

    listener.onRefsChangedEvent(e);

    verify(notifier, never()).notify(repo);
  }
}
