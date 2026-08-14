package com.studio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS for local development, so a separate frontend (e.g. Angular on :4200 in Sprint 1)
 * can call the API. The MVP UI is served from /static by this same app, so it's same-origin
 * and doesn't even need this — but it's here for when the frontend moves out.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins("*").allowedMethods("*");
    }
}
