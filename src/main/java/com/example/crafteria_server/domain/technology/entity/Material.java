package com.example.crafteria_server.domain.technology.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Material {
    FILAMENT("FILAMENT"),
    NYLONPOWDER("NYLONPOWDER"),
    METALPOWDER("METALPOWDER"),
    LIQUID("LIQUID"),
    ;
    private final String key;
}
