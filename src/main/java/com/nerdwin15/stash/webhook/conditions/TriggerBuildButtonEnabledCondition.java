package com.nerdwin15.stash.webhook.conditions;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.nerdwin15.stash.webhook.service.SettingsService;

import java.util.Map;

/**
 * Checks whether or not the "Trigger Build Button" on the Pull Requests Overview page should be displayed or not
 * Created by Stephen Liang
 */
public class TriggerBuildButtonEnabledCondition implements Condition {

    private final SettingsService settingsService;

    public static final String OMIT_TRIGGER_BUILD_BUTTON = "omitTriggerBuildButton";
    public static final String REPOSITORY = "repository";

    public TriggerBuildButtonEnabledCondition(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public void init(Map<String, String> stringStringMap) throws PluginParseException {
        // Nothing to do here
    }

    /**
     * Checks if we should display the "Trigger Build Button", true by default
     * @param context Stash Context
     * @return True by default. False if explicitly disabled
     */
    @Override
    public boolean shouldDisplay(Map<String, Object> context) {
        final Object obj = context.get(REPOSITORY);

        if (obj == null || !(obj instanceof Repository)) {
            return true;
        }

        Repository repo = (Repository) obj;

        Settings settings = settingsService.getSettings(repo);

        if (settings != null) {
            Boolean shouldOmitTriggerBuildButton = settings.getBoolean(OMIT_TRIGGER_BUILD_BUTTON);
            return shouldOmitTriggerBuildButton == null ? true : !shouldOmitTriggerBuildButton;
        }

        return true;
    }
}
