package com.nerdwin15.stash.webhook.service.eligibility;

import static com.nerdwin15.stash.webhook.service.eligibility.IgnoreCommittersEligibilityFilter.IGNORED_COMMITTERS_LIST_SPLITTING_CHARACTER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.nerdwin15.stash.webhook.Notifier;
import com.nerdwin15.stash.webhook.service.SettingsService;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test case for the {@link IgnoreCommittersEligibilityFilter} class
 *
 * @author Michael Irwin (mikesir87)
 */
@RunWith(MockitoJUnitRunner.class)
public class IgnoreCommittersEligibilityFilterTest {

    private final String username = "Pinky";
    private final String usernameWithSpace = "The Brain";
    private final String spaces = "  ";

    @Mock private SettingsService settingsService;

    @Mock private Settings settings;

    @Mock private Repository repo;

    @Mock private EventContext eventContext;

    private IgnoreCommittersEligibilityFilter filter;

    /**
     * Setup tasks
     */
    @Before
    public void setUp() throws Exception {
        when(settingsService.getSettings(repo)).thenReturn(settings);
        when(eventContext.getEventSource()).thenReturn(null);
        when(eventContext.getRepository()).thenReturn(repo);
        when(eventContext.getUsername()).thenReturn(username);

        filter = new IgnoreCommittersEligibilityFilter(settingsService);
    }

    /**
     * Validate that the filter should still allow delivery when no ignored
     * committers settings have been set.
     *
     * @throws Exception
     */
    @Test
    public void shouldAllowNotificationWhenIgnoredCommittersListIsNull() throws Exception {
        when(settings.getString(Notifier.IGNORE_COMMITTERS))
                .thenReturn(null);

        assertTrue(filter.shouldDeliverNotification(eventContext));
    }

    /**
     * Validate that the filter should still allow delivery when ignored
     * committers settings have been set to empty string.
     *
     * @throws Exception
     */
    @Test
    public void shouldAllowNotificationWhenIgnoredCommittersListIsEmpty() throws Exception {
        when(settings.getString(Notifier.IGNORE_COMMITTERS))
                .thenReturn("");

        assertTrue(filter.shouldDeliverNotification(eventContext));
    }

    /**
     * Validate that the filter should still allow delivery when the event user
     * does not match any of the ignored committers.
     *
     * @throws Exception
     */
    @Test
    public void shouldAllowNotificationWhenIgnoredCommittersDoesntMatch() throws Exception {
        when(settings.getString(Notifier.IGNORE_COMMITTERS))
                .thenReturn(username + "-notmatching");

        assertTrue(filter.shouldDeliverNotification(eventContext));
    }

    /**
     * Validate that the filter should not allow notification if an ignored committer matches.
     *
     * @throws Exception
     */
    @Test
    public void shouldNotAllowNotificationWhenIgnoredCommittersMatches() throws Exception {
        when(settings.getString(Notifier.IGNORE_COMMITTERS))
                .thenReturn(username);

        assertFalse(filter.shouldDeliverNotification(eventContext));
    }

    /**
     * Validate that the filter should not allow notification if an ignored committer matches,
     * even if name is prefixed and suffixed with spaces.
     *
     * @throws Exception
     */
    @Test
    public void shouldNotAllowNotificationWhenIgnoredCommittersMatchesDespiteOfSpaces() throws Exception {
        final String ignoredCommiter = spaces + username + spaces;
        when(settings.getString(Notifier.IGNORE_COMMITTERS))
                .thenReturn(ignoredCommiter);

        assertFalse(filter.shouldDeliverNotification(eventContext));
    }

    /**
     * Validate that the filter should not allow notification if one of ignored committers matches.
     *
     * @throws Exception
     */
    @Test
    public void shouldNotAllowNotificationWhenMatchesWithMultipleCommitters() throws Exception {
        final String ignoredCommiters = username + IGNORED_COMMITTERS_LIST_SPLITTING_CHARACTER + usernameWithSpace;
        when(settings.getString(Notifier.IGNORE_COMMITTERS))
                .thenReturn(ignoredCommiters);

        assertFalse(filter.shouldDeliverNotification(eventContext));
    }

    /**
     * Validate that the filter should not allow notification if ignored committer matches
     * and username of committer contains space.
     *
     * @throws Exception
     */
    @Test
    public void shouldNotAllowNotificationWhenMatchesUsernameContainingSpace() throws Exception {
        when(eventContext.getUsername()).thenReturn(usernameWithSpace);
        when(settings.getString(Notifier.IGNORE_COMMITTERS))
                .thenReturn(usernameWithSpace);

        assertFalse(filter.shouldDeliverNotification(eventContext));
    }

}
