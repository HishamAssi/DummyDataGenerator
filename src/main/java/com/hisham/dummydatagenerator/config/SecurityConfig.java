package com.hisham.dummydatagenerator.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the DummyDataGenerator application.
 * 
 * This configuration:
 * - Protects actuator endpoints with HTTP Basic authentication
 * - Leaves application API endpoints (/api/**) unsecured for now
 * - Uses in-memory user configured in application.properties
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Secure actuator endpoints - require ACTUATOR role
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR")
                // Require authentication for all other requests (API endpoints)
                .anyRequest().authenticated()
            )
            // Enable HTTP Basic authentication for actuator endpoints
            .httpBasic(httpBasic -> {})
            // Disable CSRF for API usage (you may want to enable this later)
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}