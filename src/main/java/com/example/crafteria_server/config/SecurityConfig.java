package com.example.crafteria_server.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {
    static String[] whiteList = {
            "/api/v1/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "v3/api-docs/**",
    };
    @Bean
    public SecurityFilterChain apiConfig(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(whiteList).permitAll()
                );

        return http.build();
    }
}
