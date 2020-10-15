package com.csye.webapp.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ImproperException extends RuntimeException{

    public ImproperException(String message) {
        super(message);
    }

}
