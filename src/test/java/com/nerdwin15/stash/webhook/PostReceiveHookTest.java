package com.nerdwin15.stash.webhook;

import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class PostReceiveHookTest {

    private static final String JENKINS_BASE_URL = "http://localhost.jenkins";
    private static final String STASH_BASE_URL = "ssh://git@some.stash.com:7999";

    private Notifier notifier;

    private PostReceiveHook hook;

    private SettingsValidationErrors errors;

    private Settings settings;

    private Repository repo;
    @Before
    public void setup() throws Exception {
        notifier = mock(Notifier.class);
        hook = new PostReceiveHook(notifier);
        settings = mock(Settings.class);
        errors = mock(SettingsValidationErrors.class);
        repo = mock(Repository.class);
    }

    @Test
    public void shouldDelegateToNotifier() throws Exception {
        RepositoryHookContext ctx = mock(RepositoryHookContext.class);

        when(ctx.getRepository()).thenReturn(repo);
        
        hook.postReceive(ctx, null);
        
        verify(notifier).notify(repo);
    }

    @Test
    public void shouldValidateSuccessfullyWithoutStashBase() throws Exception {
        when(settings.getString(Notifier.JENKINS_BASE)).thenReturn(JENKINS_BASE_URL);
        when(settings.getString(Notifier.STASH_BASE)).thenReturn(null);
        
        hook.validate(settings, errors, repo);
        
        verify(errors, never()).addFieldError(anyString(), anyString());
    }

    @Test
    public void shouldValidateSuccessfullyWithStashBase() throws Exception {
        when(settings.getString(Notifier.JENKINS_BASE)).thenReturn(JENKINS_BASE_URL);
        when(settings.getString(Notifier.STASH_BASE)).thenReturn(STASH_BASE_URL);
        
        hook.validate(settings, errors, repo);
        
        verify(errors, never()).addFieldError(anyString(), anyString());
    }

    @Test
    public void shouldFailValidationWithoutJenkinsBase() throws Exception {
        when(settings.getString(Notifier.JENKINS_BASE)).thenReturn(null);
        when(settings.getString(Notifier.STASH_BASE)).thenReturn(STASH_BASE_URL);
        
        hook.validate(settings, errors, repo);
        
        verify(errors, times(1)).addFieldError(eq(Notifier.JENKINS_BASE), anyString());
    }
}
