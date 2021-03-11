package com.example.demo;

import org.keycloak.KeycloakPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/")
    public String getRoot() {
        return "Hello world!";
    }

    @GetMapping("/tenant/{realmId}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAdmin(@PathVariable String realmId, @AuthenticationPrincipal KeycloakPrincipal token) {
        return message(realmId, token, "admin");
    }

    @GetMapping("/tenant/{realmId}/owner")
    @PreAuthorize("hasRole('OWNER')")
    public String getOwner(@PathVariable String realmId, @AuthenticationPrincipal KeycloakPrincipal token) {
        return message(realmId, token, "owner");
    }

    private String message(String realmId, KeycloakPrincipal principal, String role) {
        String name = principal.getKeycloakSecurityContext().getIdToken().getName();
        return String.format("Hello %s, you have %s-role in realm %s!", name, role, realmId);
    }

}
