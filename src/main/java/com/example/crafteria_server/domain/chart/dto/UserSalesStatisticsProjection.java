package com.example.crafteria_server.domain.chart.dto;

import java.time.LocalDate;

public interface UserSalesStatisticsProjection {
    LocalDate getSalesDate();
    Long getTotalSalesAmount();
    Integer getTotalOrders();
}

