package com.example.universityservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider") // Link to the auditor provider bean
public class AuditingConfig {

    /**
     * Configures the AuditorAware bean to provide the current auditor (user) for auditing fields.
     * This implementation assumes Spring Security is used to get the current authenticated user's name.
     * If Spring Security is not used, you'll need a different mechanism to get the current user.
     *
     * @return An Optional containing the current auditor's username, or empty if no user is authenticated.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // This is a placeholder. In a real application, you'd get the current authenticated
            // user's ID or username from Spring Security Context or a custom context.
            // For example, if using Spring Security:
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty(); // Or a default value like "SYSTEM"
            }
            return Optional.of(authentication.getName()); // Assuming the principal's name is the auditor
            // If your user object is complex, you might cast:
            // return Optional.ofNullable(((CustomUserDetails) authentication.getPrincipal()).getUserId().toString());
        };
    }
}
