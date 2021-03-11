package com.example.demo;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.facade.SimpleHttpFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class KeycloakMultitenantSecurityContextRefreshFilter extends GenericFilterBean implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakMultitenantSecurityContextRefreshFilter.class);

    private static final String FILTER_APPLIED = KeycloakMultitenantSecurityContextRefreshFilter.class.getPackage().getName() + ".refreshed";

    private ApplicationContext applicationContext;
    private AdapterDeploymentContext deploymentContext;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (request.getAttribute(FILTER_APPLIED) != null) {
            filterChain.doFilter(request, response);
            return;
        }

        request.setAttribute(FILTER_APPLIED, Boolean.TRUE);

        KeycloakSecurityContext keycloakSecurityContext = getKeycloakSecurityContext();

        if (keycloakSecurityContext instanceof RefreshableKeycloakSecurityContext) {
            RefreshableKeycloakSecurityContext refreshableSecurityContext = (RefreshableKeycloakSecurityContext) keycloakSecurityContext;
            KeycloakDeployment deployment = resolveDeployment(request, response);

            if (deployment != null) {
                String deploymentRealm = deployment.getRealm();
                String securityContextRealm = refreshableSecurityContext.getDeployment().getRealm();

                if (!deploymentRealm.equalsIgnoreCase(securityContextRealm)) {
                    LOG.warn("deploymentRealm={} != securityContextRealm={} -> clearing securityContext", deploymentRealm, securityContextRealm);
                    clearAuthenticationContext();
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected void initFilterBean() {
        deploymentContext = applicationContext.getBean(AdapterDeploymentContext.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private KeycloakSecurityContext getKeycloakSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof KeycloakPrincipal) {
                return KeycloakPrincipal.class.cast(principal).getKeycloakSecurityContext();
            }
        }

        return null;
    }

    private KeycloakDeployment resolveDeployment(ServletRequest servletRequest, ServletResponse servletResponse) {
        return deploymentContext.resolveDeployment(new SimpleHttpFacade(HttpServletRequest.class.cast(servletRequest), HttpServletResponse.class.cast(servletResponse)));
    }

    private void clearAuthenticationContext() {
        SecurityContextHolder.clearContext();
    }
}
