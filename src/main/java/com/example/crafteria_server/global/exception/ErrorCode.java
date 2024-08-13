package com.example.crafteria_server.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    InternalServerError(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    BadRequest(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    NotFound(HttpStatus.NOT_FOUND, "찾을 수 없는 리소스입니다."),
    MethodNotAllowed(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메소드입니다."),
    Conflict(HttpStatus.CONFLICT, "이미 존재하는 리소스입니다."),
    Unauthorized(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    Forbidden(HttpStatus.FORBIDDEN, "권한이 없습니다.");

    private final HttpStatus status;
    private final String message;
}
