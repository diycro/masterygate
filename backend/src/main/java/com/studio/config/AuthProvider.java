package com.studio.config;

/**
 * Seam: how the app resolves "who is the current user".
 * Sprint 0 uses a dev stub. Later sprints swap in a JWT/OAuth (or Auth0/Clerk)
 * implementation WITHOUT changing any calling code — that's the point of the interface.
 */
public interface AuthProvider {
    String currentUserId();
}
