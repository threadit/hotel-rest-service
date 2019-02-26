package com.agoda.hotel.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class HotelException extends RuntimeException{

    public HotelException(String message) {
        super(message);
    }
}

