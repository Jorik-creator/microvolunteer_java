package com.microvolunteer.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public BusinessException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(message, HttpStatus.CONFLICT, "CONFLICT");
    }
}