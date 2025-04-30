package backend.academy.scrapper.rateLimiter;

import backend.academy.scrapper.exceptionHandling.exceptions.RateLimitExceededException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@AllArgsConstructor
public class RateLimitAspect {

    private final IpRateLimiter limiter;

    @Around("@annotation(rateLimit)")
    public Object limit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getRemoteAddr();
        String endpoint = request.getRequestURI();

        RateLimiter rateLimiter = limiter.getRateLimiter(ip, endpoint);

        if (rateLimiter.acquirePermission()) {
            return joinPoint.proceed();
        } else {
            throw new RateLimitExceededException(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(), HttpStatus.TOO_MANY_REQUESTS.value());
        }
    }
}
