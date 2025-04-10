package com.example.crafteria_server.domain.chart.controller;

import com.example.crafteria_server.domain.chart.dto.MonthlySalesStatisticsDTO;
import com.example.crafteria_server.domain.chart.dto.SalesStatisticsDTO;
import com.example.crafteria_server.domain.chart.service.SalesStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chart")
@Slf4j(topic = "ChartController")
@RequiredArgsConstructor
public class SalesStatisticsController {

    private final SalesStatisticsService salesStatisticsService;

    @GetMapping("/daily")
    @Operation(summary = "특정 날짜 매출 통계 조회")
    public ResponseEntity<SalesStatisticsDTO> getSalesStatisticsByDate(
            @RequestParam Long manufacturerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate specificDate
    ) {
        SalesStatisticsDTO statistics = salesStatisticsService.getSalesStatisticsByDate(manufacturerId, specificDate);
        return ResponseEntity.ok(statistics);
    }

    // 특정 기간 매출 통계
    @Operation(summary = "특정 기간 매출 통계 조회")
    @GetMapping("/period")
    public ResponseEntity<SalesStatisticsDTO> getSalesStatisticsByPeriod(
            @RequestParam Long manufacturerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        SalesStatisticsDTO statistics = salesStatisticsService.getSalesStatisticsByPeriod(manufacturerId, startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    // 월별 매출 통계
    @Operation(summary = "월별 매출 통계 조회")
    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlySalesStatisticsDTO>> getMonthlySalesStatistics(
            @RequestParam Long manufacturerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<MonthlySalesStatisticsDTO> statistics = salesStatisticsService.getMonthlySalesStatistics(manufacturerId, startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
}


