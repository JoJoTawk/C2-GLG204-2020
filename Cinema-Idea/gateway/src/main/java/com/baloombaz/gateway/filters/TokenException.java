package com.baloombaz.gateway.filters;

import javax.servlet.ServletException;

public class TokenException extends ServletException {

    public TokenException(String message) {
        super(message);
    }

}
