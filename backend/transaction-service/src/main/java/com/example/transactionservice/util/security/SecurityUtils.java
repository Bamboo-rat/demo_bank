package com.example.transactionservice.util.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;


public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<String> getCurrentCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String customerId = jwt.getClaimAsString("customerId");
            if (customerId != null && !customerId.isBlank()) {
                return Optional.of(customerId);
            }

            String subject = jwt.getSubject();
            if (subject != null && !subject.isBlank()) {
                return Optional.of(subject);
            }
        } else if (principal instanceof String principalName) {
            if (!principalName.isBlank() && !"anonymousUser".equalsIgnoreCase(principalName)) {
                return Optional.of(principalName);
            }
        }

        String authName = authentication.getName();
        if (authName != null && !authName.isBlank() && !"anonymousUser".equalsIgnoreCase(authName)) {
            return Optional.of(authName);
        }

        return Optional.empty();
    }
}
