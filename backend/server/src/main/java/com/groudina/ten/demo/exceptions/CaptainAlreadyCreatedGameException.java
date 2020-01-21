package com.groudina.ten.demo.exceptions;

public class CaptainAlreadyCreatedGameException extends RuntimeException {
    public CaptainAlreadyCreatedGameException() {
    }

    public CaptainAlreadyCreatedGameException(String message) {
        super(message);
    }

    public CaptainAlreadyCreatedGameException(String message, Throwable cause) {
        super(message, cause);
    }

    public CaptainAlreadyCreatedGameException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
