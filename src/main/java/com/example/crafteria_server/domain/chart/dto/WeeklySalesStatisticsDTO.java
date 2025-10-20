package com.example.crafteria_server.domain.chart.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklySalesStatisticsDTO {
    private int year;
    private int week;
    private long totalSalesAmount;
    private int totalOrders;
}
