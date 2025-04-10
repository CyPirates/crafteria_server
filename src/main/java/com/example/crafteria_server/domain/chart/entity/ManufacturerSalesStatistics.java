package com.example.crafteria_server.domain.chart.entity;

import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "manufacturer_sales_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManufacturerSalesStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id", nullable = false)
    private Manufacturer manufacturer;

    @Column(nullable = false)
    private LocalDate salesDate; // 매출 발생 일자

    @Column(nullable = false)
    private long totalSalesAmount; // 매출 총액

    @Column(nullable = false)
    private int totalOrders; // 판매 건수
}
