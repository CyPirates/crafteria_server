package com.example.crafteria_server.domain.model.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Material {
    PLA("PLA"),
    ABS("ABS"),
    NYLON("NYLON"),
    WOOD("WOOD"),

    ;
    private final String key;
}
