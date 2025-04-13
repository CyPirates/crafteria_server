package com.example.crafteria_server.domain.user.entity;

import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import com.example.crafteria_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    private String oauth2Id;
    private String username;
    private String realname;
    private String provider;
    private String providerId;
    private String phoneNumber;
    private String address;

    @Column(nullable = true)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = true) // <== 이렇게 바꿔주세요
    @Enumerated(EnumType.STRING)
    private DashboardStatus dashboardStatus;


    @OneToOne(mappedBy = "dashboardUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Manufacturer manufacturer;  // 제조사와 1대1 매핑

    @Column(name = "ban_until")
    private LocalDateTime banUntil;

    @Column(nullable = true)
    private String manufacturerName;

    @Column(nullable = true)
    private String manufacturerDescription;

}
