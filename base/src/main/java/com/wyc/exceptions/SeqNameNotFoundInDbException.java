package com.wyc.exceptions;

public class SeqNameNotFoundInDbException extends RuntimeException {
    public SeqNameNotFoundInDbException(String message) {
        super(message);
    }
}
