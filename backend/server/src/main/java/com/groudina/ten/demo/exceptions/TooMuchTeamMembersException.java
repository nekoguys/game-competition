package com.groudina.ten.demo.exceptions;

public class TooMuchTeamMembersException extends RuntimeException {
    public TooMuchTeamMembersException() {
    }

    public TooMuchTeamMembersException(String message) {
        super(message);
    }
}
