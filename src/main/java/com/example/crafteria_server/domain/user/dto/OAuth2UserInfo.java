package com.example.crafteria_server.domain.user.dto;

import com.example.crafteria_server.domain.user.entity.Role;
import com.example.crafteria_server.domain.user.entity.User;
import jakarta.security.auth.message.AuthException;
import lombok.Builder;

import java.util.Map;

@Builder
public record OAuth2UserInfo(String name,
        String email
        ) {
    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return ofGoogle(attributes);
        }
    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .build();
    }
    public User toEntity() {
        return User.builder()
                .realname(name)
                .oauth2Id(email)
                .role(Role.USER)
                .build();
    }
}


