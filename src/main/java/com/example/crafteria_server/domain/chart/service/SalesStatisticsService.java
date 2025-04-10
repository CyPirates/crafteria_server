package com.example.crafteria_server.domain.chart.service;

import com.example.crafteria_server.domain.chart.dto.*;
import com.example.crafteria_server.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesStatisticsService {

    private final OrderRepository orderRepository;

    // 특정일 매출 통계
    public SalesStatisticsDTO getSalesStatisticsByDate(Long manufacturerId, LocalDate specificDate) {
        // Projection을 통해 데이터 조회
        SalesStatisticsProjection projection = orderRepository.findSalesStatisticsByDate(manufacturerId, specificDate);

        // Projection이 null일 경우 기본값 반환
        return new SalesStatisticsDTO(
                specificDate, // 요청된 날짜 그대로 반환
                projection != null && projection.getTotalSalesAmount() != null ? projection.getTotalSalesAmount() : 0,
                projection != null && projection.getTotalOrders() != null ? projection.getTotalOrders() : 0
        );
    }
    // 특정 기간 매출 통계
    public SalesStatisticsDTO getSalesStatisticsByPeriod(Long manufacturerId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        PeriodSalesStatisticsProjection projection = orderRepository.findSalesStatisticsByPeriod(
                manufacturerId, startDateTime, endDateTime
        );

        return new SalesStatisticsDTO(
                null,
                projection != null && projection.getTotalSalesAmount() != null ? projection.getTotalSalesAmount() : 0,
                projection != null && projection.getTotalOrders() != null ? projection.getTotalOrders() : 0
        );
    }

    // 월별 매출 통계
    public List<MonthlySalesStatisticsDTO> getMonthlySalesStatistics(Long manufacturerId, LocalDate startDate, LocalDate endDate) {
        // LocalDate -> LocalDateTime 변환
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<MonthlySalesStatisticsProjection> projections = orderRepository.findMonthlySalesStatistics(
                manufacturerId, startDateTime, endDateTime
        );

        return projections.stream()
                .map(projection -> new MonthlySalesStatisticsDTO(
                        projection.getYear(),
                        projection.getMonth(),
                        projection.getTotalSalesAmount() != null ? projection.getTotalSalesAmount() : 0,
                        projection.getTotalOrders() != null ? projection.getTotalOrders() : 0
                ))
                .collect(Collectors.toList());
    }
}

