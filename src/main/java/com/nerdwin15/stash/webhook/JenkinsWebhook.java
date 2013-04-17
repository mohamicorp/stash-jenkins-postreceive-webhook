package com.nerdwin15.stash.webhook;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import com.atlassian.stash.hook.repository.AsyncPostReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.RepositorySettingsValidator;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;
import com.nerdwin15.stash.webhook.service.ConcreteHttpClientFactory;
import com.nerdwin15.stash.webhook.service.HttpClientFactory;

/**
 * Note that hooks can implement RepositorySettingsValidator directly.
 */
public class JenkinsWebhook implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator {

	private HttpClientFactory factory;
	
	public JenkinsWebhook() {
		factory = new ConcreteHttpClientFactory();
	}
	
    /**
     * Connects to a configured URL to notify of all changes.
     */
    @Override
    public void postReceive(RepositoryHookContext context, 
    		Collection<RefChange> refChanges) {
        String jenkinsBase = context.getSettings().getString("jenkinsBase");
        String gitRepoUrl = context.getSettings().getString("gitRepoUrl");
        Boolean ignoreCerts = context.getSettings().getBoolean("ignoreCerts");
        
        if (jenkinsBase == null || gitRepoUrl == null)
        	return;
        
        Boolean usingSsl = jenkinsBase.startsWith("https");
        HttpClient client = null;
        
        try {
	        client = factory.getHttpClient(usingSsl, ignoreCerts);
        	HttpGet get = new HttpGet(getUrl(jenkinsBase, gitRepoUrl));
        	client.execute(get);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	if (client != null)
        		client.getConnectionManager().shutdown();
        }
    }
    
    protected String getUrl(String jenkinsBase, String gitRepoUrl) 
    		throws Exception {
    	String query = String.format("url=%s", 
    			URLEncoder.encode(gitRepoUrl, "UTF-8"));
    	return jenkinsBase.replaceFirst("/$", "") + "/git/notifyCommit?" + query;
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
    
    /**
     * Used for testing purposes
     */
    protected void setFactory(HttpClientFactory factory) {
		this.factory = factory;
	}
}
