package com.example.crafteria_server.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
public enum Role {
    USER("ROLE_USER"),
    DASHBOARD("ROLE_DASHBOARD");

    private final String key;

    Role(String key) {
        this.key = key;
    }

}
