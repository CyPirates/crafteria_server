package com.example.crafteria_server.domain.chart.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor

@Builder
public class MonthlySalesStatisticsDTO {
    private int year;                // 연도
    private int month;               // 월
    private long totalSalesAmount;   // 매출 총액
    private int totalOrders;         // 판매 건수

    public MonthlySalesStatisticsDTO(int year, int month, long totalSalesAmount, int totalOrders) {
        this.year = year;
        this.month = month;
        this.totalSalesAmount = totalSalesAmount;
        this.totalOrders = totalOrders;
    }
}
