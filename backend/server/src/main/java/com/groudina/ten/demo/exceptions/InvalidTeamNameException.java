package com.groudina.ten.demo.exceptions;

public class InvalidTeamNameException extends RuntimeException {
    public InvalidTeamNameException() {
    }

    public InvalidTeamNameException(String message) {
        super(message);
    }
}
