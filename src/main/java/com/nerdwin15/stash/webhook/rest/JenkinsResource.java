package com.nerdwin15.stash.webhook.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.rest.interceptor.ResourceContextInterceptor;
import com.atlassian.stash.rest.util.ResourcePatterns;
import com.atlassian.stash.rest.util.ResponseFactory;
import com.atlassian.stash.rest.util.RestResource;
import com.atlassian.stash.rest.util.RestUtils;
import com.atlassian.stash.ssh.api.SshCloneUrlResolver;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionValidationService;
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
@InterceptorChain(ResourceContextInterceptor.class)
public class JenkinsResource extends RestResource {

  private static final Logger log = //CHECKSTYLE:doesntMatter
      LoggerFactory.getLogger(JenkinsResource.class);

  private final Notifier notifier;
  private final PermissionValidationService permissionService;
  private final NavBuilder navBuilder;
  private final SshCloneUrlResolver sshCloneUrlResolver;

  /**
   * Creates Rest resource for testing the Jenkins configuration
   * @param notifier The service to send Jenkins notifications
   * @param permissionValidationService A permission validation service 
   * @param i18nService i18n Service
   * @param navBuilder Builder to generate the default HTTP clone url
   * @param sshCloneUrlResolver Resolver for generating default SSH clone url
   */
  public JenkinsResource(Notifier notifier, 
      PermissionValidationService permissionValidationService, 
      I18nService i18nService, 
      NavBuilder navBuilder, 
      SshCloneUrlResolver sshCloneUrlResolver) {
    super(i18nService);
    this.notifier = notifier;
    this.permissionService = permissionValidationService;
    this.navBuilder = navBuilder;
    this.sshCloneUrlResolver = sshCloneUrlResolver;
  }

  /**
   * Fire off the test of the Jenkins configuration
   * @param repository The repository to base the notification on
   * @param jenkinsBase The base URL for the Jenkins instance
   * @param cloneUrl The url used for repository cloning
   * @param ignoreCerts True if all certs should be accepted.
   * @return The response to send back to the user.
   */
  @POST
  @Path(value = "test")
  public Response test(@Context Repository repository,
        @FormParam(Notifier.JENKINS_BASE) String jenkinsBase,
        @FormParam(Notifier.CLONE_URL) String cloneUrl,
        @FormParam(Notifier.IGNORE_CERTS) boolean ignoreCerts) {
    
    if (jenkinsBase == null || cloneUrl == null)
      return Response.status(Status.BAD_REQUEST).build();
    
    permissionService.validateForRepository(repository, Permission.REPO_ADMIN);
    log.debug("Triggering jenkins notification for repository {}/{}", 
        repository.getProject().getKey(), repository.getSlug());

    final String response = notifier.notify(repository, jenkinsBase, 
        ignoreCerts, cloneUrl);
    log.debug("Got response from jenkins: {}", response);
    if (response == null || !response.startsWith("Scheduled")) {
      return fail(repository);
    }
    log.info("Successfully triggered jenkins for repository {}/{}", 
        repository.getProject().getKey(), repository.getSlug());
    return Response.ok().build();
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
    data.put("ssh", sshCloneUrlResolver.getCloneUrl(repository));
    data.put("http", navBuilder.repo(repository).clone("git")
        .buildAbsoluteWithoutUsername());
    return Response.ok(data).build();
  }

  private Response fail(final Repository repository) {
    log.info("Triggering jenkins failed for repository {}/{}", 
        repository.getProject().getKey(), repository.getSlug());
    return ResponseFactory.ok("FAIL").build();
  }
  
}