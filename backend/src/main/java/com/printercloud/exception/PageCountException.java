package com.printercloud.exception;

public class PageCountException extends RuntimeException {
    public PageCountException(String message) {
        super(message);
    }
    public PageCountException(String message, Throwable cause) {
        super(message, cause);
    }
}

