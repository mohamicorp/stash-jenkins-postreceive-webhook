package com.nerdwin15.stash.webhook;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;

import com.atlassian.stash.hook.repository.AsyncPostReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.RepositorySettingsValidator;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;

/**
 * Note that hooks can implement RepositorySettingsValidator directly.
 */
public class JenkinsWebhook implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator {

    /**
     * Connects to a configured URL to notify of all changes.
     */
    @Override
    public void postReceive(RepositoryHookContext context, 
    		Collection<RefChange> refChanges) {
        String jenkinsBase = context.getSettings().getString("jenkinsBase");
        String gitRepoUrl = context.getSettings().getString("gitRepoUrl");
        
        if (jenkinsBase != null && gitRepoUrl != null) {
	        try {
	        	String query = String.format("url=%s", 
	        		     URLEncoder.encode(gitRepoUrl, "UTF-8"));
		        String url = jenkinsBase + "/git/notifyCommit?" + query;
	            URLConnection connection = new URL(url).openConnection();
	            connection.getInputStream();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
        }
    }

    @Override
    public void validate(Settings settings, SettingsValidationErrors errors, Repository repository) {
        if (settings.getString("jenkinsBase", "").isEmpty()) {
            errors.addFieldError("jenkinsBase", "The url for your Jenkins Instance is required.");
        }
        if (settings.getString("gitRepoUrl", "").isEmpty()) {
            errors.addFieldError("gitRepoUrl", "The url for the Git repository is required.");
        }
    }
    
}
