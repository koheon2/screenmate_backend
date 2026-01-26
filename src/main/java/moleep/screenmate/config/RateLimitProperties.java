package moleep.screenmate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rate-limit.llm")
@Getter
@Setter
public class RateLimitProperties {

    private int requestsPerMinute = 60;
}
