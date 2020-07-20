package com.groudina.ten.demo.exceptions;

public class IllegalGameRestartException extends Exception {

    public IllegalGameRestartException() {
    }

    public IllegalGameRestartException(String message) {
        super(message);
    }
}
