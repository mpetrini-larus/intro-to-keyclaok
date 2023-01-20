package com.example.keycloak.keycloakDemo.api;

import com.example.keycloak.keycloakDemo.domain.KeyUser;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class KeycloakUserResource {
    private static final String DEFAULT_PASSWORD = "ajeje";

    @GetMapping("/test")
    public ResponseEntity test(){
        return ResponseEntity.ok().build();
    }


    @GetMapping("/users")
    public ResponseEntity getUsers(){
        try (Keycloak keycloak = getKeycloakClient()){
            return ResponseEntity.ok()
                    .body(
                            keycloak.realm("demo-realm").users().list()
                    );
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    @PutMapping("/users")
    public ResponseEntity creteUser(@RequestBody KeyUser keyUser){
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(keyUser.getUsername());
        userRepresentation.setFirstName(keyUser.getFirstName());
        userRepresentation.setLastName(keyUser.getLastName());
        userRepresentation.setEnabled(true);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(DEFAULT_PASSWORD);
        credentialRepresentation.setTemporary(true);
        userRepresentation.setCredentials(List.of(credentialRepresentation));

        userRepresentation.setRequiredActions(List.of("UPDATE_PASSWORD"));

        try (Keycloak keycloak = getKeycloakClient()) {
            Response response = keycloak.realm("demo-realm").users().create(userRepresentation);
            return response.getStatus() == 201 ?
                    ResponseEntity.created(response.getLocation()).build():
                    ResponseEntity.status(response.getStatus()).build()
                    ;
        } catch (Exception exception) {
            return handleException(exception);
        }
    }

    private Keycloak getKeycloakClient(){
        return KeycloakBuilder.builder()
                .serverUrl("http://keycloak:8888")
                .realm("demo-realm")
                .clientId( "rest-api-cli")
                .clientSecret("9AflKJ8JxYLUmzPXIruYQyJqCjlBmiap")
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

    private ResponseEntity handleException(Exception exception){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        Map.of(
                                "error", ExceptionUtils.getMessage(exception),
                                "stack-trace", ExceptionUtils.getStackTrace(exception)
                        )
                );
    }

}
