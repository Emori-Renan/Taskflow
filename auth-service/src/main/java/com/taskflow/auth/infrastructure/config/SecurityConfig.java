package com.taskflow.auth.infrastructure.config; // <-- Package must match your file path

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the REST microservice.
 * This explicitly enables custom security and allows all necessary public paths.
 */
@Configuration
@EnableWebSecurity // <--- CRITICAL: This enables custom security and disables default behavior.
public class SecurityConfig {

    private static final String[] WHITE_LIST_URLS = {
            // Public APIs (e.g., /api/auth/register, /api/auth/login)
            "/api/auth/**", 
            // OpenAPI UI Paths
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // 1. Disable CSRF for stateless REST APIs
            .csrf(AbstractHttpConfigurer::disable)

            // 2. Configure request authorization
            .authorizeHttpRequests(authorize -> authorize
                // Allow public access to all paths listed above (Auth endpoints and OpenAPI)
                .requestMatchers(WHITE_LIST_URLS).permitAll()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .build();
    }
}