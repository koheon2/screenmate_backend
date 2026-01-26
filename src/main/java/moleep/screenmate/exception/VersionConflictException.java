package moleep.screenmate.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class VersionConflictException extends BaseException {

    private final Long expectedVersion;
    private final Long actualVersion;

    public VersionConflictException(Long expectedVersion, Long actualVersion) {
        super(HttpStatus.CONFLICT, "VERSION_CONFLICT",
                String.format("Version conflict: expected %d but found %d", expectedVersion, actualVersion));
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }
}
