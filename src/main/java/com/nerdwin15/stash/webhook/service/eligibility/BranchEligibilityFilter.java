package com.nerdwin15.stash.webhook.service.eligibility;

import com.atlassian.stash.event.RepositoryPushEvent;
import com.atlassian.stash.setting.Settings;
import com.nerdwin15.stash.webhook.Notifier;
import com.nerdwin15.stash.webhook.service.BranchEvaluator;
import com.nerdwin15.stash.webhook.service.SettingsService;

/**
 * Defines an eligibility filter that provides the ability to create a 
 * black/whitelist of branches to ignore or accept commits for notification
 * sending.
 * 
 * @author Michael Irwin (mikesir87)
 */
public class BranchEligibilityFilter 
    implements EligibilityFilter {

  private SettingsService settingsService;
  private BranchEvaluator branchEvaluator;
  
  /**
   * Create a new instance.
   * @param settingsService The settings service
   * @param branchEvaluator An evaluator to determine affected branches.
   */
  public BranchEligibilityFilter(SettingsService settingsService, 
      BranchEvaluator branchEvaluator) {
    this.settingsService = settingsService;
    this.branchEvaluator = branchEvaluator;
  }
  
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldDeliverNotification(EventContext context) {
    if (!RepositoryPushEvent.class.isAssignableFrom(context.getEventSource()
        .getClass()))
      return true;
    
    RepositoryPushEvent event = (RepositoryPushEvent) context.getEventSource();

    final Settings settings = settingsService.getSettings(
        context.getRepository());
    String branchOption = settings.getString(Notifier.BRANCH_OPTIONS);
    if (branchOption == null || 
        (!branchOption.equals("blacklist") && !branchOption.equals("whitelist")))
      return true;
    
    String[] branchesSettings = 
        settings.getString(Notifier.BRANCH_OPTIONS_BRANCHES).split(" ");
    Iterable<String> branches = 
        branchEvaluator.getBranches(event.getRefChanges());
    
    boolean haveMatch = hasMatch(branchesSettings, branches);
    if (haveMatch && branchOption.equals("blacklist"))
      return false;
    else if (!haveMatch && branchOption.equals("whitelist"))
      return false;
    return true;
  }
  
  protected boolean hasMatch(String[] settings, 
      Iterable<String> affectedBranches) {
    for (String branch : affectedBranches) {
      branch = branch.toLowerCase();
      for (String s : settings) {
        s = s.toLowerCase();
        if (s.endsWith("*") && branch.startsWith(s.substring(0, s.length() - 1)))
          return true;
        if (s.equals(branch))
          return true;
      }
    }
    
    return false;
  }

}
