package com.gideon.task_management_api.exception;


import org.springframework.http.HttpStatus;

import lombok.Getter;

public abstract class ApiException extends RuntimeException {

    @Getter
    private final HttpStatus status;

    protected ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
