package com.example.crafteria_server.domain.chart.controller;

import com.example.crafteria_server.domain.chart.dto.UserMonthlySalesStatisticsDTO;
import com.example.crafteria_server.domain.chart.dto.UserSalesStatisticsDTO;
import com.example.crafteria_server.domain.chart.service.UserSalesStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/statistics/user")
@Tag(name = "유저 도면 판매 통계")
public class UserSalesStatisticsController {

    private final UserSalesStatisticsService service;

    @GetMapping("/daily")
    @Operation(summary = "특정일 도면 판매 통계 조회")
    public ResponseEntity<UserSalesStatisticsDTO> getDaily(
            @RequestParam Long authorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(service.getDaily(authorId, date));
    }

    @GetMapping("/period")
    @Operation(summary = "특정기간 도면 판매 통계 조회")
    public ResponseEntity<UserSalesStatisticsDTO> getPeriod(
            @RequestParam Long authorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(service.getPeriod(authorId, start, end));
    }

    @GetMapping("/monthly")
    @Operation(summary = "월별 도면 판매 통계 조회")
    public ResponseEntity<List<UserMonthlySalesStatisticsDTO>> getMonthly(
            @RequestParam Long authorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(service.getMonthly(authorId, start, end));
    }

    @GetMapping("/weekly")
    @Operation(summary = "주간 도면 판매 통계 조회")
    public ResponseEntity<List<Map<String, Object>>> getWeekly(
            @RequestParam Long authorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(service.getWeekly(authorId, start, end));
    }
}
