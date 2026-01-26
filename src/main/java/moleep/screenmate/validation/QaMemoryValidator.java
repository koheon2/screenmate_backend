package moleep.screenmate.validation;

import moleep.screenmate.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class QaMemoryValidator {

    private static final Set<String> ALLOWED_KEY_PREFIXES = Set.of(
            "user_", "pref_", "fact_", "memory_", "context_"
    );
    private static final int MAX_KEY_LENGTH = 100;
    private static final int MAX_VALUE_LENGTH = 2000;
    private static final int MAX_ENTRIES = 100;

    public void validateQaPatch(Map<String, String> qaPatch) {
        if (qaPatch == null || qaPatch.isEmpty()) {
            return;
        }

        if (qaPatch.size() > MAX_ENTRIES) {
            throw new BadRequestException("TOO_MANY_QA_ENTRIES",
                    String.format("Cannot have more than %d QA entries in a single patch", MAX_ENTRIES));
        }

        for (Map.Entry<String, String> entry : qaPatch.entrySet()) {
            validateKey(entry.getKey());
            validateValue(entry.getValue());
        }
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new BadRequestException("INVALID_QA_KEY", "QA key cannot be empty");
        }

        if (key.length() > MAX_KEY_LENGTH) {
            throw new BadRequestException("QA_KEY_TOO_LONG",
                    String.format("QA key must not exceed %d characters", MAX_KEY_LENGTH));
        }

        boolean hasValidPrefix = ALLOWED_KEY_PREFIXES.stream()
                .anyMatch(key::startsWith);

        if (!hasValidPrefix) {
            throw new BadRequestException("INVALID_QA_KEY_PREFIX",
                    String.format("QA key must start with one of: %s", ALLOWED_KEY_PREFIXES));
        }
    }

    private void validateValue(String value) {
        if (value != null && value.length() > MAX_VALUE_LENGTH) {
            throw new BadRequestException("QA_VALUE_TOO_LONG",
                    String.format("QA value must not exceed %d characters", MAX_VALUE_LENGTH));
        }
    }
}
