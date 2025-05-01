package backend.academy.scrapper.rateLimiter;

import backend.academy.scrapper.ScrapperConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class IpRateLimiter {

    private final Map<String, RateLimiter> rateLimiters;

    private final RateLimiterConfig rateLimiterConfig;

    private final RateLimiterRegistry registry;

    public IpRateLimiter(ScrapperConfig config) {
        rateLimiterConfig = RateLimiterConfig.custom()
                .limitForPeriod(config.rateLimit().limit())
                .limitRefreshPeriod(Duration.ofSeconds(config.rateLimit().refreshPeriodSeconds()))
                .build();
        registry = RateLimiterRegistry.of(rateLimiterConfig);
        rateLimiters = new ConcurrentHashMap<>();
    }

    public RateLimiter getRateLimiter(String ip, String endpoint) {
        return rateLimiters.computeIfAbsent(
                ip + "-" + endpoint, ignored -> registry.rateLimiter(ip + "-" + endpoint, rateLimiterConfig));
    }
}
