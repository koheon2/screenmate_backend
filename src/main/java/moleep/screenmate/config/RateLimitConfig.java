package moleep.screenmate.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitConfig {

    private final RateLimitProperties rateLimitProperties;
    private final Map<UUID, Bucket> userBuckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(UUID userId) {
        return userBuckets.computeIfAbsent(userId, this::newBucket);
    }

    private Bucket newBucket(UUID userId) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(rateLimitProperties.getRequestsPerMinute())
                .refillGreedy(rateLimitProperties.getRequestsPerMinute(), Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public boolean tryConsume(UUID userId) {
        return resolveBucket(userId).tryConsume(1);
    }

    public long getAvailableTokens(UUID userId) {
        return resolveBucket(userId).getAvailableTokens();
    }
}
