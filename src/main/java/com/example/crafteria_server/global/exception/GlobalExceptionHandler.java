package com.example.crafteria_server.global.exception;

import com.example.crafteria_server.global.response.ResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Slf4j(topic = "GlobalExceptionHandler")
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(NullPointerException.class)
    public ResponseBody handleNullPointerException(NullPointerException e){
        log.error(e.getMessage());
        log.error(Arrays.toString(e.getStackTrace()));
        return ErrorResponse.toResponse(ErrorCode.InternalServerError);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseBody handleResponseStatusException(ResponseStatusException e){
        log.error(e.getMessage());
        log.error(Arrays.toString(e.getStackTrace()));
        return ErrorResponse.toResponse(e);
    }
}
