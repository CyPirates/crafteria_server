package com.example.crafteria_server.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModelPurchaseRequest {
    private Long userId;
    private Long modelId;
    private Long couponId; // nullable (쿠폰 없을 경우 null)
}
