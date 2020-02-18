package com.groudina.ten.demo.exceptions;

public class IllegalAnswerSubmissionException extends RuntimeException {
    public IllegalAnswerSubmissionException(String message) {
        super(message);
    }
}
