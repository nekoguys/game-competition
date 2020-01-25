package com.groudina.ten.demo.exceptions;

public class UserTriedToJoinManyTeamsException extends RuntimeException {
    public UserTriedToJoinManyTeamsException() {
    }

    public UserTriedToJoinManyTeamsException(String message) {
        super(message);
    }
}
