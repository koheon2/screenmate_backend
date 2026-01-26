package moleep.screenmate.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends BaseException {

    public InvalidTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", message, cause);
    }
}
