package com.example.crafteria_server.domain.chart.dto;

public interface MonthlySalesStatisticsProjection {
    Integer getYear();          // 연도
    Integer getMonth();         // 월
    Long getTotalSalesAmount(); // 매출 총액
    Integer getTotalOrders();   // 판매 건수

}
