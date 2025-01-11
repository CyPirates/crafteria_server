package com.example.crafteria_server.domain.order.entity;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "file_id")
    private File file;

    @Column(nullable = true)
    private double widthSize;

    @Column(nullable = true)
    private double lengthSize;

    @Column(nullable = true)
    private double heightSize;

    @Column(nullable = true)
    private double magnification;

    @Column(nullable = true)
    private int quantity;
}
