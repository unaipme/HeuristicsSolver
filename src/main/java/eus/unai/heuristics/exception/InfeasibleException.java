package eus.unai.heuristics.exception;

public class InfeasibleException extends RuntimeException {

    public InfeasibleException() {
        super("No feasible solution for the provided instance");
    }
}
