package com.example.crafteria_server.domain.chart.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserMonthlySalesStatisticsDTO {
    private int year;
    private int month;
    private long totalSalesAmount;
    private int totalOrders;
}
