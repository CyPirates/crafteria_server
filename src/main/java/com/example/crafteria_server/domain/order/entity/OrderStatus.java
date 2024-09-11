package com.example.crafteria_server.domain.order.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    ORDERED("ORDERED"),
    IN_PRODUCTING("IN_PRODUCTING"),
    DELIVERED("DELIVERED"),
    CANCELED("CANCELED"),

    ;
    private final String key;
}
