package com.example.demo;

import org.apache.http.HttpHeaders;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationEntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.example.demo.TenantPathResolver.extractTenantId;

public class KeycloakMultitenantAuthenticationEntryPoint extends KeycloakAuthenticationEntryPoint {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakMultitenantAuthenticationEntryPoint.class);

    public KeycloakMultitenantAuthenticationEntryPoint(AdapterDeploymentContext adapterDeploymentContext) {
        super(adapterDeploymentContext);
    }

    public KeycloakMultitenantAuthenticationEntryPoint(AdapterDeploymentContext adapterDeploymentContext, RequestMatcher apiRequestMatcher) {
        super(adapterDeploymentContext, apiRequestMatcher);
    }

    private static String realmAwareLoginUri(HttpServletRequest request) {
        return String.format("%s/tenant/%s%s", request.getContextPath(), extractTenantId(request.getRequestURI()), DEFAULT_LOGIN_URI);
    }

    @Override
    protected void commenceLoginRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String realmAwareLoginUri = realmAwareLoginUri(request);
        LOG.info("redirecting to login uri={}", realmAwareLoginUri);
        response.sendRedirect(realmAwareLoginUri);
    }

    @Override
    protected void commenceUnauthorizedResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.addHeader(HttpHeaders.WWW_AUTHENTICATE, String.format("Bearer realm=\"%s\"", extractTenantId(request.getRequestURI())));
        response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }
}