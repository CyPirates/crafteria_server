package com.example.crafteria_server.domain.identity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyResponse {
    private String status;      // "VERIFIED"
    private String name;
    private String phoneNumber; // null이면 응답에서 제외
    private String verifiedAt;
}
