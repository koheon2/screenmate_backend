package moleep.screenmate.exception;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends BaseException {

    public RateLimitExceededException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", message);
    }
}
