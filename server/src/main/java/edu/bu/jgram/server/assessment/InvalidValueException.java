package edu.bu.jgram.server.assessment;

public class InvalidValueException extends Exception {
    private static final long serialVersionUID = 2865407904004122099L;

    public InvalidValueException(String errorMessage) {
        super(errorMessage);
    }
}
