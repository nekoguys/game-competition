package com.groudina.ten.demo.exceptions;

public class IllegalStrategySubmissionException extends RuntimeException {
    public IllegalStrategySubmissionException() {
        super();
    }

    public IllegalStrategySubmissionException(String message) {
        super(message);
    }
}
