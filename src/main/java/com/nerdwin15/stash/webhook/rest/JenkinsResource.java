package com.nerdwin15.stash.webhook.rest;

import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.rest.interceptor.ResourceContextInterceptor;
import com.atlassian.stash.rest.util.ResourcePatterns;
import com.atlassian.stash.rest.util.ResponseFactory;
import com.atlassian.stash.rest.util.RestResource;
import com.atlassian.stash.rest.util.RestUtils;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionValidationService;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import com.nerdwin15.stash.webhook.Notifier;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.spi.resource.Singleton;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Path(ResourcePatterns.REPOSITORY_URI)
@Consumes({MediaType.APPLICATION_JSON})
@Produces({RestUtils.APPLICATION_JSON_UTF8})
@Singleton
@AnonymousAllowed
@InterceptorChain(ResourceContextInterceptor.class)
public class JenkinsResource extends RestResource {

    private static final Logger log = LoggerFactory.getLogger(JenkinsResource.class);

    private final Notifier notifier;
    private final PermissionValidationService permissionValidationService;

    public JenkinsResource(Notifier notifier, PermissionValidationService permissionValidationService, I18nService i18nService) {
        super(i18nService);
        this.notifier = notifier;
        this.permissionValidationService = permissionValidationService;
    }

    @POST
    @Path(value = "test")
    public Response test(@Context Repository repository,
                         @FormParam(Notifier.JENKINS_BASE) String jenkinsBase,
                         @FormParam(Notifier.STASH_BASE) String stashBase,
                         @FormParam(Notifier.IGNORE_CERTS) boolean ignoreCerts) {
        permissionValidationService.validateForRepository(repository, Permission.REPO_ADMIN);
        log.debug("Triggering jenkins notification for repository {}/{}", repository.getProject().getKey(), repository.getSlug());

        final String response = notifier.notify(repository, jenkinsBase, stashBase, ignoreCerts);
        log.debug("Got response from jenkins: {}", response);
        if (response == null || !response.startsWith("Scheduled")) {
            return fail(repository);
        }
        log.info("Successfully triggered jenkins for repository {}/{}", repository.getProject().getKey(), repository.getSlug());
        return Response.ok().build();

    }

    private Response fail(final Repository repository) {
        log.info("Triggering jenkins failed for repository {}/{}", repository.getProject().getKey(), repository.getSlug());
        return ResponseFactory.ok("FAIL").build();
    }
}