package com.studio.config;

import org.springframework.stereotype.Component;

/**
 * Sprint 0 stub implementation of {@link AuthProvider}.
 * Returns a fixed dev user so the app runs with no login yet.
 * Replace with a real JWT/OAuth provider in a later sprint (the seam stays the same).
 */
@Component
public class DevAuthProvider implements AuthProvider {
    @Override
    public String currentUserId() {
        return "dev-user";
    }
}
