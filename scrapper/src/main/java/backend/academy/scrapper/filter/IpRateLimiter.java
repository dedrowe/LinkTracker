package backend.academy.scrapper.filter;

import backend.academy.scrapper.ScrapperConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IpRateLimiter {

    private final Map<String, RateLimiter> rateLimiters;

    private final RateLimiterConfig rateLimiterConfig;

    private final RateLimiterRegistry registry;

    public IpRateLimiter(ScrapperConfig config) {
        rateLimiterConfig = RateLimiterConfig.custom()
            .limitForPeriod(config.rateLimit().limit())
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .build();
        registry = RateLimiterRegistry.of(rateLimiterConfig);
        rateLimiters = new ConcurrentHashMap<>();
    }

    private RateLimiter getBucket(String ip, String endpoint) {
        return rateLimiters.computeIfAbsent(ip + "-" + endpoint, ignored -> registry.rateLimiter(ip + "-" + endpoint, rateLimiterConfig));
    }

    public boolean tryExecute(String ip, String endpoint, Runnable task) {
        RateLimiter rateLimiter = getBucket(ip, endpoint);
        try {
            rateLimiter.executeRunnable(task);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
