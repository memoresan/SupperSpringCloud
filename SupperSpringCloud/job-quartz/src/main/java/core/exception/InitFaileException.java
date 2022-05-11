package core.exception;

public class InitFaileException extends Exception {

    public InitFaileException() {
    }

    public InitFaileException(String message) {
        super(message);
    }

    public InitFaileException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitFaileException(Throwable cause) {
        super(cause);
    }

}
