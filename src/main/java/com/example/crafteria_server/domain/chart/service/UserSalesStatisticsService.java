package com.example.crafteria_server.domain.chart.service;

import com.example.crafteria_server.domain.chart.dto.UserMonthlySalesStatisticsDTO;
import com.example.crafteria_server.domain.chart.dto.UserSalesStatisticsDTO;
import com.example.crafteria_server.domain.model.repository.ModelPurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSalesStatisticsService {
    private final ModelPurchaseRepository modelPurchaseRepository;

    public UserSalesStatisticsDTO getDaily(Long authorId, LocalDate date) {
        var projection = modelPurchaseRepository.findDailySales(authorId, date);
        return projection == null ? new UserSalesStatisticsDTO(date, 0, 0)
                : new UserSalesStatisticsDTO(date,
                projection.getTotalSalesAmount() != null ? projection.getTotalSalesAmount() : 0,
                projection.getTotalOrders() != null ? projection.getTotalOrders() : 0);
    }

    public UserSalesStatisticsDTO getPeriod(Long authorId, LocalDate start, LocalDate end) {
        var projection = modelPurchaseRepository.findPeriodSales(authorId, start, end);
        return projection == null ? new UserSalesStatisticsDTO(null, 0, 0)
                : new UserSalesStatisticsDTO(null,
                projection.getTotalSalesAmount() != null ? projection.getTotalSalesAmount() : 0,
                projection.getTotalOrders() != null ? projection.getTotalOrders() : 0);
    }

    public List<UserMonthlySalesStatisticsDTO> getMonthly(Long authorId, LocalDate start, LocalDate end) {
        var projections = modelPurchaseRepository.findMonthlySales(authorId, start, end);
        return projections.stream()
                .map(p -> new UserMonthlySalesStatisticsDTO(
                        p.getYear(), p.getMonth(),
                        p.getTotalSalesAmount() != null ? p.getTotalSalesAmount() : 0,
                        p.getTotalOrders() != null ? p.getTotalOrders() : 0
                )).toList();
    }

    public List<Map<String, Object>> getWeekly(Long authorId, LocalDate start, LocalDate end) {
        List<Object[]> rows = modelPurchaseRepository.findWeeklySales(authorId, start, end);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(Map.of(
                    "week", row[0],
                    "totalSalesAmount", row[1] != null ? row[1] : 0,
                    "totalOrders", row[2] != null ? row[2] : 0
            ));
        }
        return result;
    }
}
