package hu.agilexpert.exception;

public class AgileExpertException extends RuntimeException {
    public AgileExpertException(String message) {
        super(message);
    }

    public AgileExpertException(String message, Throwable cause) {
        super(message, cause);
    }
}
