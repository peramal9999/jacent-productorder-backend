package com.jacent.storefront.utils;

import com.jacent.storefront.pojo.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Returns the currently authenticated User from the SecurityContext.
     * Throws IllegalStateException if:
     * - SecurityContext has no authentication
     * - Authentication is not fully authenticated (e.g. anonymous)
     * - Principal is not an instance of User
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in SecurityContext");
        }

        Object principal = authentication.getPrincipal();

        if (principal == null || "anonymousUser".equals(principal)) {
            throw new IllegalStateException("Anonymous users are not allowed to perform this action");
        }

        if (!(principal instanceof User)) {
            throw new IllegalStateException("Unexpected principal type: " + principal.getClass().getName());
        }

        return (User) principal;
    }
}
