package com.nerdwin15.stash.webhook.service.eligibility;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import com.atlassian.bitbucket.repository.RefChangeType;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.event.repository.RepositoryPushEvent;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.nerdwin15.stash.webhook.Notifier;
import com.nerdwin15.stash.webhook.service.BranchEvaluator;
import com.nerdwin15.stash.webhook.service.SettingsService;

/**
 * Test case for the {@link BranchEligibilityFilter} filter.
 *
 * @author Michael Irwin (mikesir87)
 */
public class BranchEligibilityFilterTest {

  private SettingsService settingsService;
  private Settings settings;
  private BranchEvaluator branchEvaluator;
  private BranchEligibilityFilter filter;
  private Repository repo;
  private RepositoryPushEvent event;
  private EventContext eventContext;
  private String branches = "ignoreMe wild*";
  private Collection<RefChange> changes = mock(LinkedList.class);
  private Iterator<RefChange> iterChanges = mock(Iterator.class);
  private RefChange change = mock(RefChange.class);
  private RefChangeType changeTypeUpdate = RefChangeType.UPDATE;
  private RefChangeType changeTypeDelete = RefChangeType.DELETE;
  
  /**
   * Perform setup tasks
   */
  @Before
  public void setUp() {
    settingsService = mock(SettingsService.class);
    settings = mock(Settings.class);
    branchEvaluator = mock(BranchEvaluator.class);
    event = mock(RepositoryPushEvent.class);
    repo = mock(Repository.class);
    eventContext = mock(EventContext.class);

    when(change.getType()).thenReturn(changeTypeUpdate);
    when(iterChanges.next()).thenReturn(change);
    when(changes.iterator()).thenReturn(iterChanges);
    when(event.getRefChanges()).thenReturn(changes);
    when(eventContext.getEventSource()).thenReturn(event);
    when(eventContext.getRepository()).thenReturn(repo);
    when(settingsService.getSettings(repo)).thenReturn(settings);
    when(settings.getString(Notifier.BRANCH_OPTIONS)).thenReturn("blacklist");
    when(settings.getString(Notifier.BRANCH_OPTIONS_BRANCHES))
        .thenReturn(branches);
    
    filter = new BranchEligibilityFilter(settingsService, branchEvaluator);    
  }
  
  /**
   * Validate that the matcher works as expected
   */
  @Test
  public void testHasMatchMethod() {
    assertTrue(filter.hasMatch(array("master"), iterable("master")));
    assertTrue(filter.hasMatch(array("mas*"), iterable("master")));
    assertFalse(filter.hasMatch(array("master"), iterable("develop")));
    assertTrue(filter.hasMatch(array("master", "deve*"), iterable("develop")));
    assertFalse(filter.hasMatch(array("mas*", "dev*"), iterable("issue")));
    assertTrue(filter.hasMatch(array("MASTER", "DEVE*"), iterable("develop")));
    assertTrue(filter.hasMatch(array("master", "deve*"), iterable("DEVELOP")));
  }
  
  /**
   * Validate that if another event type is provided, the filter doesn't process
   * it.
   */
  @Test
  public void testEnsureOnlyWorksWithRepositoryPushEvents() {
    when(eventContext.getEventSource()).thenReturn("Something else");
    assertTrue(filter.shouldDeliverNotification(eventContext));
  }
  
  /**
   * Validate that if no branch option (blacklist/whitelist) was provided, the
   * filter passes the notification.
   */
  @Test
  public void testEnsureIfNoBranchOptionIsProvidedFilterPasses() {
    when(settings.getString(Notifier.BRANCH_OPTIONS)).thenReturn(null);
    assertTrue(filter.shouldDeliverNotification(eventContext));
    
    when(settingsService.getSettings(repo)).thenReturn(settings);
    when(settings.getString(Notifier.BRANCH_OPTIONS)).thenReturn("somethingElse");
    assertTrue(filter.shouldDeliverNotification(eventContext));
  }
  
  /**
   * Ensure that if a branch is blacklisted, a notification isn't sent for it.
   */
  @Test
  public void testBlacklistingWorks() {
    when(branchEvaluator.getBranches(changes)).thenReturn(iterable("wildCard"));
    assertFalse(filter.shouldDeliverNotification(eventContext));

    when(branchEvaluator.getBranches(changes)).thenReturn(iterable("asdf"));
    assertTrue(filter.shouldDeliverNotification(eventContext));
  }
  
  /**
   * Ensure that if a branch is blacklisted, a notification isn't sent for it.
   */
  @Test
  public void testWhitelistingWorks() {
    when(settings.getString(Notifier.BRANCH_OPTIONS)).thenReturn("whitelist");
    when(branchEvaluator.getBranches(changes)).thenReturn(iterable("wildCard"));
    assertTrue(filter.shouldDeliverNotification(eventContext));

    when(branchEvaluator.getBranches(changes)).thenReturn(iterable("asdf"));
    assertFalse(filter.shouldDeliverNotification(eventContext));
  }

  /**
   * Ensure that if a branch is deleted, a notification isn't sent for it.
   */
  @Test
  public void testEnsureDeleteBranchNoNotification() {
    when(change.getType()).thenReturn(changeTypeDelete);
    when(iterChanges.next()).thenReturn(change);
    when(changes.iterator()).thenReturn(iterChanges);
    when(event.getRefChanges()).thenReturn(changes);
    when(eventContext.getEventSource()).thenReturn(event);
    assertFalse(filter.shouldDeliverNotification(eventContext));
  }
  
  private String[] array(String... elements) {
    return elements;
  }
  
  private List<String> iterable(String... elements) {
    return new ArrayList<String>(Arrays.asList(elements));
  }
  
}
