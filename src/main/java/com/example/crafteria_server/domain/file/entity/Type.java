package com.example.crafteria_server.domain.file.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Type {
    IMAGE("TYPE_IMAGE"),
    MODEL("TYPE_MODEL"),
    ;

    private final String key;
}
