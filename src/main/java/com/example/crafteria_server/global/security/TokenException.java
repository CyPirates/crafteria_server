package com.example.crafteria_server.global.security;

import com.example.crafteria_server.global.exception.CustomException;
import com.example.crafteria_server.global.exception.ErrorCode;

public class TokenException extends CustomException {

    public TokenException(ErrorCode errorCode) {
        super(errorCode);
    }
}