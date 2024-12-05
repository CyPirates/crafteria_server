package com.example.crafteria_server.domain.user.entity;

import com.example.crafteria_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = true)
    private String dashboardStatus; // "PENDING" or "APPROVED"

    @Builder
    public User(String oauth2Id, String username, String realname, Role role, String dashboardStatus) {
        this.oauth2Id = oauth2Id;
        this.username = username;
        this.realname = realname;
        this.role = role;
        this.dashboardStatus = dashboardStatus;
    }

}
