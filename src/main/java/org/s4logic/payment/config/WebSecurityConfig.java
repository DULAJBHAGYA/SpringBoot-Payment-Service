package org.s4logic.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Web Security Configurations
 *
 * @author Prasad Madusanka
 * @since 25 January 2026
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * Security Filter Chain
     *
     * @param http org.springframework.security.config.annotation.web.builders.HttpSecurity
     * @return org.springframework.security.web.SecurityFilterChain
     * @throws Exception java.lang.Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable) // CORS handled by gateway
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/payment/**", "/v1/stripe/**", "/actuator/**").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }
}

