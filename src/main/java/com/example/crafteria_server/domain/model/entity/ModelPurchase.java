package com.example.crafteria_server.domain.model.entity;

import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "model_purchase")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ModelPurchase extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @Column(nullable = true, unique = true)
    private String paymentId; // 결제 ID 추가

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;  // 결제 완료 여부
}
