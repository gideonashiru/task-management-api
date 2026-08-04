package com.gideon.task_management_api.exception;

import org.springframework.http.HttpStatus;

public class InvalidStateException extends ApiException {

    public InvalidStateException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
