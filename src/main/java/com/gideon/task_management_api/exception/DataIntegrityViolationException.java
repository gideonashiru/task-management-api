package com.gideon.task_management_api.exception;

import org.springframework.http.HttpStatus;

public class DataIntegrityViolationException extends ApiException {

    public DataIntegrityViolationException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
