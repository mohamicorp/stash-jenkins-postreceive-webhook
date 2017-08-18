package com.nerdwin15.stash.webhook.service.eligibility;

import com.atlassian.bitbucket.event.branch.BranchCreatedEvent;
import com.atlassian.bitbucket.event.repository.RepositoryRefsChangedEvent;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.nerdwin15.stash.webhook.service.BranchEvaluator;
import com.nerdwin15.stash.webhook.service.SettingsService;
import org.junit.Before;
import org.junit.Test;


import static com.nerdwin15.stash.webhook.Notifier.OMIT_NEW_BRANCH_WITHOUT_CHANGES;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test case for the {@link NewBranchEligibilityFilter} filter.
 *
 * @author Simon Schuster (Setre14)
 */
public class NewBranchEligibilityFilterTest {

  private SettingsService settingsService;
  private Settings settings;
  private BranchEvaluator branchEvaluator;
  private NewBranchEligibilityFilter filter;
  private Repository repo;
  private RepositoryRefsChangedEvent event;
  private EventContext eventContext;


  /**
   * Perform setup tasks
   */
  @Before
  public void setUp() {
    settingsService = mock(SettingsService.class);
    settings = mock(Settings.class);
    branchEvaluator = mock(BranchEvaluator.class);

    repo = mock(Repository.class);
    eventContext = mock(EventContext.class);

    when(eventContext.getRepository()).thenReturn(repo);
    when(settingsService.getSettings(repo)).thenReturn(settings);



    filter = new NewBranchEligibilityFilter(settingsService);
  }

  /**
   * Validates that the filter returns true if the filter is not activated
   */
  @Test
  public void testNoNewBranchFilter(){
    event = mock(BranchCreatedEvent.class);
    when(settings.getBoolean(OMIT_NEW_BRANCH_WITHOUT_CHANGES, false)).thenReturn(false);
    assertTrue(filter.shouldDeliverNotification(eventContext));
  }

  /**
   * Validates that the filter returns false, if the event was a BranchCreatedEvent (=> new Branch was created in Bitbucket)
   */
  @Test
  public void testIsBranchCreatedEvent(){
    event = mock(BranchCreatedEvent.class);
    when(settings.getBoolean(OMIT_NEW_BRANCH_WITHOUT_CHANGES, false)).thenReturn(true);
    when(eventContext.getEventSource()).thenReturn(event);
    assertFalse(filter.shouldDeliverNotification(eventContext));
  }

  /**
   * Validates that the filter returns true, if the was not a BranchCreatedEvent
   */
  @Test
  public void testIsNotBranchCreatedEvent(){
    event = mock(RepositoryRefsChangedEvent.class);
    when(settings.getBoolean(OMIT_NEW_BRANCH_WITHOUT_CHANGES, false)).thenReturn(true);
    when(eventContext.getEventSource()).thenReturn(event);
    assertTrue(filter.shouldDeliverNotification(eventContext));
  }
}
