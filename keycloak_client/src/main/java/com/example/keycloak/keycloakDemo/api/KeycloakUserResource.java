package com.example.keycloak.keycloakDemo.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class KeycloakUserResource {

    @GetMapping("/test")
    public ResponseEntity test(){
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/roles")
    public ResponseEntity getUserRoles(){
        return ResponseEntity.ok()
                .body(
                        SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                );
    }

}
