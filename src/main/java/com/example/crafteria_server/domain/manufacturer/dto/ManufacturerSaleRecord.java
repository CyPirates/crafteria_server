package com.example.crafteria_server.domain.manufacturer.dto;

import com.example.crafteria_server.domain.order.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManufacturerSaleRecord {
    @Schema(description = "주문 id", example = "1")
    private Long orderId;

    @Schema(description = "판매 가격", example = "100000")
    private long purchasePrice;

    @Schema(description = "구매자 이름" , example = "홍길동")
    private String recipientName;

    @Schema(description = "판매 일시", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    public static ManufacturerSaleRecord from(Order order) {
        return ManufacturerSaleRecord.builder()
                .orderId(order.getId())
                .purchasePrice(order.getPurchasePrice())
                .recipientName(order.getRecipientName())
                .createdAt(order.getCreateDate())
                .build();
    }
}
