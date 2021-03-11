package com.example.demo;


import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@KeycloakConfiguration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MultitenantWebSecurityConfigurerAdapter extends KeycloakWebSecurityConfigurerAdapter {

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(keycloakAuthenticationProvider());
    }

    @Override
    protected AuthenticationEntryPoint authenticationEntryPoint() throws Exception {
        return new KeycloakMultitenantAuthenticationEntryPoint(adapterDeploymentContext());
    }

    @Override
    protected KeycloakAuthenticationProcessingFilter keycloakAuthenticationProcessingFilter() throws Exception {
        KeycloakAuthenticationProcessingFilter filter = new KeycloakAuthenticationProcessingFilter(
                authenticationManager(),
                new AntPathRequestMatcher("/tenant/*/sso/login"));
        filter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy());
        return filter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.authorizeRequests().anyRequest().authenticated();
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Override
    @Bean
    public KeycloakAuthenticationProvider keycloakAuthenticationProvider() {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = new KeycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(grantedAuthoritiesMapper());
        return keycloakAuthenticationProvider;
    }

    @Bean
    public KeycloakConfigResolver keycloakConfigResolver() {
        return new KeycloakMultitenantConfigResolver();
    }

    @Bean
    public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        // All roles have ROLE_-prefix and roles from keycloak are mapped
        // to upper case style to be spring security-conform
        SimpleAuthorityMapper authorityMapper = new SimpleAuthorityMapper();
        authorityMapper.setConvertToUpperCase(true);
        return authorityMapper;
    }

    @Bean
    public KeycloakMultitenantSecurityContextRefreshFilter keycloakMultitenantSecurityContextRefreshFilter() {
        return new KeycloakMultitenantSecurityContextRefreshFilter();
    }
}
