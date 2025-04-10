package com.example.crafteria_server.domain.chart.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class SalesStatisticsDTO {
    private LocalDate salesDate;      // 매출 발생 일자
    private long totalSalesAmount;   // 매출 총액
    private int totalOrders;         // 판매 건수

    // JPQL에서 사용하는 생성자
    public SalesStatisticsDTO(LocalDate salesDate, long totalSalesAmount, int totalOrders) {
        this.salesDate = salesDate;
        this.totalSalesAmount = totalSalesAmount;
        this.totalOrders = totalOrders;
    }
}