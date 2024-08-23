package com.example.crafteria_server.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Builder
    public User(String oauth2Id, String username, String realname, Role role) {
        this.oauth2Id = oauth2Id;
        this.username = username;
        this.realname = realname;
        this.role = role;
    }
}
