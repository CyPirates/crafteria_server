package com.example.crafteria_server.domain.user.repository;

import com.example.crafteria_server.domain.model.entity.ModelPurchase;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ModelSaleTransaction(
        Long modelId,
        String modelName,
        long price,
        String buyerUsername,
        LocalDateTime purchasedAt
) {
    public static ModelSaleTransaction from(ModelPurchase purchase) {
        return ModelSaleTransaction.builder()
                .modelId(purchase.getModel().getId())
                .modelName(purchase.getModel().getName())
                .price(purchase.getModel().getPrice())
                .buyerUsername(purchase.getUser().getUsername())
                .purchasedAt(purchase.getCreateDate())
                .build();
    }
}