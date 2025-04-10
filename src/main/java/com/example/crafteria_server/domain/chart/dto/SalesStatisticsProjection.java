package com.example.crafteria_server.domain.chart.dto;

import java.time.LocalDate;

public interface SalesStatisticsProjection {
    LocalDate getSalesDate();       // 매출 발생 일자
    Long getTotalSalesAmount();     // 매출 총액
    Integer getTotalOrders();           // 판매 건수
}
