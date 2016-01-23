package com.nerdwin15.stash.webhook.conditions;

import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.nerdwin15.stash.webhook.service.SettingsService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static com.nerdwin15.stash.webhook.conditions.TriggerBuildButtonEnabledCondition.REPOSITORY;
import static com.nerdwin15.stash.webhook.conditions.TriggerBuildButtonEnabledCondition.OMIT_TRIGGER_BUILD_BUTTON;
import static org.mockito.Mockito.when;

public class TriggerBuildButtonEnabledConditionTest {
    @Mock
    private SettingsService settingsService;
    @Mock
    private Repository repository;
    @Mock
    private Settings settings;

    private TriggerBuildButtonEnabledCondition triggerBuildButtonEnabledCondition;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        triggerBuildButtonEnabledCondition = new TriggerBuildButtonEnabledCondition(settingsService);
    }

    @Test
    public void testShouldDisplayButObjIsNull() throws Exception {
        Map<String, Object> context = new HashMap<String, Object>();
        boolean result = triggerBuildButtonEnabledCondition.shouldDisplay(context);

        assertTrue(result);
    }

    @Test
    public void testShouldDisplayButObjIsNotARepository() throws Exception {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put(REPOSITORY, "");
        boolean result = triggerBuildButtonEnabledCondition.shouldDisplay(context);

        assertTrue(result);
    }

    @Test
    public void testShouldDisplayButSettingServiceReturnedNull() throws Exception {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put(REPOSITORY, repository);

        when(settingsService.getSettings(repository)).thenReturn(null);

        boolean result = triggerBuildButtonEnabledCondition.shouldDisplay(context);

        assertTrue(result);
    }

    @Test
    public void testShouldDisplayButNotEnabled() throws Exception {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put(REPOSITORY, repository);

        when(settingsService.getSettings(repository)).thenReturn(settings);
        when(settings.getBoolean(OMIT_TRIGGER_BUILD_BUTTON)).thenReturn(true);

        boolean result = triggerBuildButtonEnabledCondition.shouldDisplay(context);

        assertFalse(result);
    }

    @Test
    public void testShouldDisplayButNull() throws Exception {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put(REPOSITORY, repository);

        when(settingsService.getSettings(repository)).thenReturn(settings);
        when(settings.getBoolean(OMIT_TRIGGER_BUILD_BUTTON)).thenReturn(null);

        boolean result = triggerBuildButtonEnabledCondition.shouldDisplay(context);

        assertTrue(result);
    }

    @Test
    public void testShouldDisplayEnabled() throws Exception {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put(REPOSITORY, repository);

        when(settingsService.getSettings(repository)).thenReturn(settings);
        when(settings.getBoolean(OMIT_TRIGGER_BUILD_BUTTON)).thenReturn(false);

        boolean result = triggerBuildButtonEnabledCondition.shouldDisplay(context);

        assertTrue(result);
    }
}