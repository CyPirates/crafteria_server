package com.example.crafteria_server.domain.chart.dto;

public interface UserMonthlySalesStatisticsProjection {
    Integer getYear();
    Integer getMonth();
    Long getTotalSalesAmount();
    Integer getTotalOrders();
}
