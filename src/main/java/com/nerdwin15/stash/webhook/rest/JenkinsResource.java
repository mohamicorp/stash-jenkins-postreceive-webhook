package com.nerdwin15.stash.webhook.rest;

import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionValidationService;
import com.atlassian.bitbucket.repository.Branch;
import com.atlassian.bitbucket.repository.RefService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.rest.RestResource;
import com.atlassian.bitbucket.rest.util.ResourcePatterns;
import com.atlassian.bitbucket.rest.util.RestUtils;
import com.atlassian.bitbucket.scm.http.HttpScmProtocol;
import com.atlassian.bitbucket.scm.ssh.SshScmProtocol;
import com.atlassian.bitbucket.ssh.SshConfiguration;
import com.atlassian.bitbucket.ssh.SshConfigurationService;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.nerdwin15.stash.webhook.NotificationResult;
import com.nerdwin15.stash.webhook.Notifier;
import com.sun.jersey.spi.resource.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.Map;

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
  private final SshConfigurationService sshConfigurationService;
  private final SshScmProtocol sshScmProtocol;
  private final HttpScmProtocol httpScmProtocol;
  private final RefService refService;

  /**
   * Creates Rest resource for testing the Jenkins configuration
   * @param notifier The service to send Jenkins notifications
   * @param permissionValidationService A permission validation service
   * @param i18nService i18n Service
   * @param sshConfigurationService Service to check whether SSH is enabled
   * @param sshScmProtocol Resolver for generating default SSH clone url
   * @param httpScmProtocol Resolver for generating default http clone url
   * @param refService Service to get default Branch
   */
  public JenkinsResource(Notifier notifier,
                         PermissionValidationService permissionValidationService,
                         I18nService i18nService,
                         SshConfigurationService sshConfigurationService,
                         SshScmProtocol sshScmProtocol,
                         HttpScmProtocol httpScmProtocol,
                         RefService refService) {
    super(i18nService);
    this.notifier = notifier;
    this.permissionService = permissionValidationService;
    this.sshConfigurationService = sshConfigurationService;
    this.sshScmProtocol = sshScmProtocol;
    this.httpScmProtocol = httpScmProtocol;
    this.refService = refService;
  }

  /**
   * Fire off the test of the Jenkins configuration
   * @param repository The repository to base the notification on
   * @param jenkinsBase The base URL for the Jenkins instance
   * @param cloneType The clone type for repository cloning
   * @param cloneUrl The url used for repository cloning
   * @param ignoreCerts True if all certs should be accepted.
   * @param omitHashCode True if SHA1 hash should be omitted.
   * @param omitBranchName True if branch name should be omitted.
   * @param omitTargetBranch True if the PR destination branch should be omitted
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
        @FormParam(Notifier.OMIT_HASH_CODE) boolean omitHashCode,
        @FormParam(Notifier.OMIT_BRANCH_NAME) boolean omitBranchName,
        @FormParam(Notifier.OMIT_TARGET_BRANCH) boolean omitTargetBranch) {

    if (jenkinsBase == null || cloneType == null || (cloneType.equals("custom") && cloneUrl == null)) {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("successful", false);
      map.put("message", "Settings must be configured");
      return map;
    }

    permissionService.validateForRepository(repository, Permission.REPO_ADMIN);
    log.debug("Triggering jenkins notification for repository {}/{}",
        repository.getProject().getKey(), repository.getSlug());

    // use default branch for test
    Branch defaultBranch = refService.getDefaultBranch(repository);
    NotificationResult result = notifier.notify(repository, jenkinsBase,
        ignoreCerts, cloneType, cloneUrl, defaultBranch.getDisplayId(),
        defaultBranch.getLatestCommit(), defaultBranch.getDisplayId(), 
        omitHashCode, omitBranchName, omitTargetBranch);
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
      @QueryParam("branches") String branches, @QueryParam("sha1") String sha1,
      @QueryParam("targetBranch") String targetBranch) {

    try {
      NotificationResult result = notifier.notify(repository, branches, sha1, targetBranch);
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
        data.put("ssh", sshScmProtocol.getCloneUrl(repository, null));
    } else {
        data.put("ssh", "");
    }
    data.put("http", httpScmProtocol.getCloneUrl(repository, null));
    return Response.ok(data).build();
  }

}
