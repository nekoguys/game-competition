package com.groudina.ten.demo.exceptions;

public class WrongTeamJoinPasswordException extends RuntimeException {
    public WrongTeamJoinPasswordException() {
    }

    public WrongTeamJoinPasswordException(String message) {
        super(message);
    }
}
