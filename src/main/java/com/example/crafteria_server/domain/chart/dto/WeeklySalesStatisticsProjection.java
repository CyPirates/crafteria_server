package com.example.crafteria_server.domain.chart.dto;

public interface WeeklySalesStatisticsProjection {
    Integer getWeek();                 // ISO 주차
    Integer getYear();                // 연도
    Long getTotalSalesAmount();       // 매출 총액
    Integer getTotalOrders();         // 판매 건수
}
