package com.wyc.sequence.base.exceptions;

public class SeqNameNotFoundInDbException extends RuntimeException {
    public SeqNameNotFoundInDbException(String message) {
        super(message);
    }
}
