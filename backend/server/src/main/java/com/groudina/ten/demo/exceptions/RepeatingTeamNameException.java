package com.groudina.ten.demo.exceptions;

public class RepeatingTeamNameException extends RuntimeException {
    public RepeatingTeamNameException() {
    }

    public RepeatingTeamNameException(String message) {
        super(message);
    }
}
