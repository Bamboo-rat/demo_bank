package com.example.customerservice.service.impl;

import com.example.customerservice.dto.request.CustomerLoginDTO;
import com.example.customerservice.dto.request.CustomerRegisterRequest;
import com.example.customerservice.exception.AuthenticationException;
import com.example.customerservice.exception.CustomerAlreadyExistsException;
import com.example.customerservice.exception.ErrorCode;
import com.example.customerservice.exception.KeycloakException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {

    private final Keycloak keycloakAdminClient;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${keycloak.resource}")
    private String loginClientId;

    @Value("${keycloak.customer.client-secret}")
    private String loginClientSecret;


    public String createUser(CustomerRegisterRequest userDto) {
        RealmResource realmResource = keycloakAdminClient.realm(realm);
        UsersResource usersResource = realmResource.users();
        
        // Check if user already exists by username or email
        List<UserRepresentation> existingUsers = usersResource.searchByUsername(userDto.getPhoneNumber(), true);
        if (!existingUsers.isEmpty()) {
            log.warn("User already exists in Keycloak with username: {}", userDto.getPhoneNumber());
            throw new CustomerAlreadyExistsException("phoneNumber", userDto.getPhoneNumber());
        }
        
        List<UserRepresentation> existingEmailUsers = usersResource.searchByEmail(userDto.getEmail(), true);
        if (!existingEmailUsers.isEmpty()) {
            log.warn("User already exists in Keycloak with email: {}", userDto.getEmail());
            throw new CustomerAlreadyExistsException("email", userDto.getEmail());
        }
        
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(userDto.getPhoneNumber());
        user.setEmail(userDto.getEmail());
        user.setEmailVerified(true);
        setFirstAndLastName(user, userDto.getFullName());

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(userDto.getPassword());
        user.setCredentials(Collections.singletonList(passwordCred));

        Response response = null;

        try {
            response = usersResource.create(user);

            if (response.getStatus() != 201) {
                String errorBody = response.readEntity(String.class);
                throw new KeycloakException(
                        ErrorCode.KEYCLOAK_USER_CREATION_FAILED,
                        Map.of("details", "Không thể tạo người dùng trên Keycloak", "keycloakResponse", errorBody),
                        null
                );
            }

            log.info("Tao nguoi dung thanh cong tren Keycloak voi username: {}", user.getUsername());

            String userId = usersResource.searchByUsername(userDto.getPhoneNumber(), true).get(0).getId();
            addRoleToUser(userId, "USER");

            return userId;
        }
        catch (ProcessingException e) {
            throw new KeycloakException(ErrorCode.KEYCLOAK_CONNECTION_ERROR, Map.of("details", "Không thể kết nối đến máy chủ Keycloak"), e);
        }
        catch (Exception e) {
            if (e instanceof KeycloakException) {
                throw e;
            }
            throw new KeycloakException(ErrorCode.KEYCLOAK_USER_CREATION_FAILED, Map.of("details", "Lỗi không mong muốn xảy ra trong quá trình tạo người dùng"), e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private void addRoleToUser(String userId, String roleName) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realm);
            RoleRepresentation roleToAdd = realmResource.roles().get(roleName).toRepresentation();
            realmResource.users().get(userId).roles().realmLevel().add(List.of(roleToAdd));
            log.info("Da gan vai tro '{}' cho nguoi dung co ID '{}'", roleName, userId);
        } catch (Exception e) {
            throw new KeycloakException(ErrorCode.KEYCLOAK_USER_UPDATE_FAILED, Map.of("details", "Không thể gán vai trò '" + roleName + "' cho người dùng " + userId), e);
        }
    }

    private void setFirstAndLastName(UserRepresentation user, String fullName) {
        if (!StringUtils.hasText(fullName)) return;
        String[] nameParts = fullName.trim().split("\\s+");
        if (nameParts.length > 1) {
            user.setLastName(nameParts[nameParts.length - 1]);
            user.setFirstName(fullName.replace(user.getLastName(), "").trim());
        } else {
            user.setFirstName(fullName);
        }
    }

    public void deleteUser(String userId) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realm);
            Response response = realmResource.users().delete(userId);

            if (response.getStatus() == 204) {
                log.info("Successfully deleted user from Keycloak with ID: {}", userId);
            } else {
                log.warn("Failed to delete user from Keycloak. Status: {}", response.getStatus());
            }
            response.close();
        } catch (Exception e) {
            log.error("Error deleting user from Keycloak with ID: {}", userId, e);
            throw new KeycloakException(ErrorCode.KEYCLOAK_USER_UPDATE_FAILED,
                Map.of("details", "Cannot delete user from Keycloak", "userId", userId), e);
        }
    }

    public Object getToken(CustomerLoginDTO loginDto) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", loginClientId);
            map.add("client_secret", loginClientSecret);
            map.add("username", loginDto.getUsername());
            map.add("password", loginDto.getPassword());
            map.add("grant_type", "password");

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
            String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            ResponseEntity<Object> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Object.class);
            log.info("Lay token thanh cong cho nguoi dung: {}", loginDto.getUsername());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.warn("Dang nhap that bai cho nguoi dung: {}. Ly do: {}", loginDto.getUsername(), e.getResponseBodyAsString());
            throw new AuthenticationException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    public Object refreshToken(String refreshToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", loginClientId);
            map.add("client_secret", loginClientSecret);
            map.add("refresh_token", refreshToken);
            map.add("grant_type", "refresh_token");

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
            String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            ResponseEntity<Object> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Object.class);
            log.info("Refresh token thanh cong");
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.warn("Refresh token that bai. Ly do: {}", e.getResponseBodyAsString());
            throw new AuthenticationException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    public void updateUserAttribute(String userId, String attributeName, String attributeValue) {
        UsersResource usersResource = keycloakAdminClient.realm(realm).users();
        UserRepresentation user = usersResource.get(userId).toRepresentation();

        if (user.getAttributes() == null) {
            user.setAttributes(new HashMap<>());
        }
        user.getAttributes().put(attributeName, List.of(attributeValue));

        usersResource.get(userId).update(user);
    }

}