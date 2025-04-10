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
    DELIVERING("DELIVERING"),
    PRODUCTED("PRODUCTED"),
    PAID("PAID"),
    ;
    private final String key;
}
