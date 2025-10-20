package com.example.crafteria_server.domain.chart.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSalesStatisticsDTO {
    private LocalDate salesDate;      // 판매 일자
    private long totalSalesAmount;    // 총 판매 금액
    private int totalOrders;          // 총 판매 건수
}
