package com.app.BookStore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the BookStore application.
 *
 * <p><b>Rules:</b>
 * <ul>
 *   <li>DELETE /books/id/** → requires ADMIN role</li>
 *   <li>All other API endpoints → open (no auth required)</li>
 *   <li>Actuator endpoints → open</li>
 * </ul>
 *
 * <p><b>In-memory users (credentials for testing):</b>
 * <pre>
 *   username: user   password: user123   role: USER
 *   username: admin  password: admin123  role: ADMIN
 * </pre>
 *
 * <p>Authentication method: HTTP Basic
 * (send Authorization: Basic &lt;base64&gt; header, or use Postman's Basic Auth tab)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ── Password encoder ──────────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ── In-memory users ───────────────────────────────────────────────────

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails regularUser = User.builder()
                .username("user")
                .password(encoder.encode("user123"))
                .roles("USER")
                .build();

        UserDetails adminUser = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")          // ADMIN also inherits all USER permissions
                .build();

        return new InMemoryUserDetailsManager(regularUser, adminUser);
    }

    // ── HTTP security rules ───────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF – REST API uses stateless tokens / Basic auth
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // ── Admin-only: DELETE ─────────────────────────────────
                .requestMatchers(HttpMethod.DELETE, "/books/id/**").hasRole("ADMIN")

                // ── Everything else is public ──────────────────────────
                .anyRequest().permitAll()
            )

            // Enable HTTP Basic authentication
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
