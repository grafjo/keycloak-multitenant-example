package com.example.demo;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class KeycloakMultitenantConfigResolver implements KeycloakConfigResolver {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakMultitenantConfigResolver.class);

    private final ConcurrentHashMap<String, KeycloakDeployment> cache = new ConcurrentHashMap<>();

    @Override
    public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {
        final String realmId = TenantPathResolver.extractTenantId(request.getRelativePath());
        LOG.debug("reading keycloak config for realm={}", realmId);
        cache.computeIfAbsent(realmId, createKeycloakDeployment());
        return cache.get(realmId);
    }

    private Function<String, KeycloakDeployment> createKeycloakDeployment() {
        return realmId -> {
            LOG.info("No keycloak config for realm={} found in cache!", realmId);
            try (InputStream is = new FileInputStream(ResourceUtils.getFile(String.format("classpath:realms/%s.json", realmId)))) {
                return KeycloakDeploymentBuilder.build(is);
            } catch (IOException e) {
                throw new IllegalStateException("Could not find file for realm=" + realmId);
            }
        };
    }
}
