package com.example.crafteria_server.domain.chart.dto;

public interface PeriodSalesStatisticsProjection {
    Long getTotalSalesAmount(); // 매출 총액
    Integer getTotalOrders();   // 판매 건수

}
