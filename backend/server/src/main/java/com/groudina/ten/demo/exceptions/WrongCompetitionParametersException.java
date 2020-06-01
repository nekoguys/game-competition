package com.groudina.ten.demo.exceptions;

public class WrongCompetitionParametersException extends RuntimeException {
    private static final long serialVersionUID = -5738499102304160874L;

    public WrongCompetitionParametersException() {
    }

    public WrongCompetitionParametersException(String message) {
        super(message);
    }

    public WrongCompetitionParametersException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongCompetitionParametersException(Throwable cause) {
        super(cause);
    }

    public WrongCompetitionParametersException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
