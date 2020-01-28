package com.groudina.ten.demo.exceptions;

public class TooMuchTeamsInCompetitionException extends RuntimeException {
    public TooMuchTeamsInCompetitionException() {
    }

    public TooMuchTeamsInCompetitionException(String message) {
        super(message);
    }
}
