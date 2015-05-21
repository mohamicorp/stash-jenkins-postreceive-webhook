package com.nerdwin15.stash.webhook.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.rest.util.ResourcePatterns;
import com.atlassian.stash.rest.util.RestResource;
import com.atlassian.stash.rest.util.RestUtils;
import com.atlassian.stash.ssh.api.SshCloneUrlResolver;
import com.atlassian.stash.ssh.api.SshConfiguration;
import com.atlassian.stash.ssh.api.SshConfigurationService;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionValidationService;
import com.nerdwin15.stash.webhook.NotificationResult;
import com.nerdwin15.stash.webhook.Notifier;
import com.sun.jersey.spi.resource.Singleton;

/**
 * REST resource used to test the Jenkins configuration
 * 
 * @author Peter Leibiger (kuhnroyal)
 * @author Michael Irwin (mikesir87)
 */
@Path(ResourcePatterns.REPOSITORY_URI)
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ RestUtils.APPLICATION_JSON_UTF8 })
@Singleton
@AnonymousAllowed
public class JenkinsResource extends RestResource {

  private static final Logger log = //CHECKSTYLE:doesntMatter
      LoggerFactory.getLogger(JenkinsResource.class);

  private final Notifier notifier;
  private final PermissionValidationService permissionService;
  private final NavBuilder navBuilder;
  private final SshConfigurationService sshConfigurationService;
  private final SshCloneUrlResolver sshCloneUrlResolver;

  /**
   * Creates Rest resource for testing the Jenkins configuration
   * @param notifier The service to send Jenkins notifications
   * @param permissionValidationService A permission validation service 
   * @param i18nService i18n Service
   * @param navBuilder Builder to generate the default HTTP clone url
   * @param sshConfigurationService Service to check whether SSH is enabled
   * @param sshCloneUrlResolver Resolver for generating default SSH clone url
   */
  public JenkinsResource(Notifier notifier, 
      PermissionValidationService permissionValidationService, 
      I18nService i18nService, 
      NavBuilder navBuilder, 
      SshConfigurationService sshConfigurationService,
      SshCloneUrlResolver sshCloneUrlResolver) {
    super(i18nService);
    this.notifier = notifier;
    this.permissionService = permissionValidationService;
    this.navBuilder = navBuilder;
    this.sshConfigurationService = sshConfigurationService;
    this.sshCloneUrlResolver = sshCloneUrlResolver;
  }

  /**
   * Fire off the test of the Jenkins configuration
   * @param repository The repository to base the notification on
   * @param jenkinsBase The base URL for the Jenkins instance
   * @param cloneType The clone type for repository cloning
   * @param cloneUrl The url used for repository cloning
   * @param ignoreCerts True if all certs should be accepted.
   * @param omitHashCode True if SHA1 hash should be omitted.
   * @return The response to send back to the user.
   */
  @POST
  @Path(value = "test")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> test(@Context Repository repository,
        @FormParam(Notifier.JENKINS_BASE) String jenkinsBase,
        @FormParam(Notifier.CLONE_TYPE) String cloneType,
        @FormParam(Notifier.CLONE_URL) String cloneUrl,
        @FormParam(Notifier.IGNORE_CERTS) boolean ignoreCerts,
        @FormParam(Notifier.OMIT_HASH_CODE) boolean omitHashCode) {
    
    if (jenkinsBase == null || cloneType == null || (cloneType.equals("custom") && cloneUrl == null)) {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("successful", false);
      map.put("message", "Settings must be configured");
      return map;
    }

    permissionService.validateForRepository(repository, Permission.REPO_ADMIN);
    log.debug("Triggering jenkins notification for repository {}/{}", 
        repository.getProject().getKey(), repository.getSlug());

    /* @todo carl.loa.odin@klarna.com: Send null instead of master and sha1 and
     *   handle this in notify
     */
    NotificationResult result = notifier.notify(repository, jenkinsBase, 
        ignoreCerts, cloneType, cloneUrl, null, null, omitHashCode, true);
    log.debug("Got response from jenkins: {}", result);

    // Shouldn't have to do this but the result isn't being marshalled correctly
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("successful", result.isSuccessful());
    map.put("url", result.getUrl());
    map.put("message", result.getMessage());
    return map;
  }
  
  /**
   * Trigger a build on the Jenkins instance
   * @param repository The repository to trigger
   * @return The response. Ok if it worked. Otherwise, an error.
   */
  @POST
  @Path(value = "triggerJenkins")
  public Response trigger(@Context Repository repository,
      @QueryParam("branch") String branch, @QueryParam("sha1") String sha1) {

    try {
      NotificationResult result = notifier.notify(repository, branch, sha1);
      if (result.isSuccessful())
        return Response.ok().build();
      return Response.noContent().build();
    }
    catch (Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(e.getMessage()).build();
    }
  }
  
  /**
   * Get the default clone urls for a repository.
   * @param repository The repository to get clone urls for
   * @return A response
   */
  @GET
  @Path(value = "config")
  public Response config(@Context Repository repository) {
    Map<String, String> data = new HashMap<String, String>();
    SshConfiguration sshConfiguration = sshConfigurationService.getConfiguration();
    if (sshConfiguration.isEnabled()) {
        data.put("ssh", sshCloneUrlResolver.getCloneUrl(repository));
    } else {
        data.put("ssh", "");
    }
    data.put("http", navBuilder.repo(repository).clone("git")
        .buildAbsoluteWithoutUsername());
    return Response.ok(data).build();
  }

}