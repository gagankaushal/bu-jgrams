package edu.bu.jgram.server.assessment;

public class InvalidGrammarException extends Exception {

    private static final long serialVersionUID = 1410614545056130753L;

    public InvalidGrammarException(String errorMessage) {
        super(errorMessage);
    }
}
