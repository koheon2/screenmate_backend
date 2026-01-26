package moleep.screenmate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    private String secret;
    private Duration accessTokenExpiry = Duration.ofMinutes(15);
    private Duration refreshTokenExpiry = Duration.ofDays(30);

    public long getAccessTokenExpiryMillis() {
        return accessTokenExpiry.toMillis();
    }

    public long getRefreshTokenExpiryMillis() {
        return refreshTokenExpiry.toMillis();
    }
}
