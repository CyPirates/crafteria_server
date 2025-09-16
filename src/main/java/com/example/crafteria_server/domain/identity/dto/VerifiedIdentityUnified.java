package com.example.crafteria_server.domain.identity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerifiedIdentityUnified {
    private String id;          // identity-verification-...
    private String status;      // VERIFIED
    private String name;

    private String phoneNumber; // v2 기본값 없음 → null 가능
    private String birth;       // yyyyMMdd
    private String gender;      // MALE/FEMALE 등
    private Boolean foreigner;  // 정보 없으면 null
    private String ci;
    private String di;
    private String operator;    // 정보 없으면 null

    private String verifiedAt;  // ISO string
}
