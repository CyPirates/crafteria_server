package com.example.crafteria_server.global.security;

import com.example.crafteria_server.global.exception.CustomException;
import com.example.crafteria_server.global.exception.ErrorCode;

public class AuthException extends CustomException {
    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }
}
