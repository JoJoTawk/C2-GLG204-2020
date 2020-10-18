package com.baloombaz.authservice.exception;


public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
