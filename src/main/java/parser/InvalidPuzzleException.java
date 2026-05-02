package parser;

public class InvalidPuzzleException extends Exception {
    public InvalidPuzzleException(String message) {
        super(message);
    }

    public InvalidPuzzleException(String message, Throwable cause) {
        super(message, cause);
    }
}
