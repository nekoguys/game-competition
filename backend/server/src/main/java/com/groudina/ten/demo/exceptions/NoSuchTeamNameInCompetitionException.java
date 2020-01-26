package com.groudina.ten.demo.exceptions;

public class NoSuchTeamNameInCompetitionException extends RuntimeException {
    public NoSuchTeamNameInCompetitionException() {
    }

    public NoSuchTeamNameInCompetitionException(String message) {
        super(message);
    }
}
