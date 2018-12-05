package eus.unai.heuristics.exception;

public class InstanceParseException extends RuntimeException {

    public InstanceParseException() {
        super("The instance could not be parsed, the syntax may be wrong or data may be missing");
    }
}
