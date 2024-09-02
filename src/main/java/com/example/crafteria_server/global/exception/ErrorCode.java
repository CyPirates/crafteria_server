package com.example.crafteria_server.global.exception;

import jakarta.annotation.Generated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;


@Getter
@AllArgsConstructor
public enum ErrorCode {
    InternalServerError(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    BadRequest(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    NotFound(HttpStatus.NOT_FOUND, "찾을 수 없는 리소스입니다."),
    MethodNotAllowed(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메소드입니다."),
    Conflict(HttpStatus.CONFLICT, "이미 존재하는 리소스입니다."),
    Unauthorized(UNAUTHORIZED, "인증이 필요합니다."),
    Forbidden(HttpStatus.FORBIDDEN, "권한이 없습니다."),

    //auth
    ILLEGAL_REGISTRATION_ID(NOT_ACCEPTABLE, "illegal registration id"),
    TOKEN_EXPIRED(UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN(UNAUTHORIZED, "올바르지 않은 토큰입니다."),
    INVALID_JWT_SIGNATURE(UNAUTHORIZED, "잘못된 JWT 시그니처입니다.");

    private final HttpStatus httpStatus;
    private final String message;

}
