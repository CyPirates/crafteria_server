package com.example.crafteria_server.domain.delivery.entity;

import com.example.crafteria_server.domain.order.entity.Order;
import com.example.crafteria_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Delivery extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "courier", nullable = false, length = 255)
    private String courier;  // 택배사

    @Column(name = "tracking_number", nullable = false, length = 255)
    private String trackingNumber;  // 송장번호


}
