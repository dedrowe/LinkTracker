package backend.academy.scrapper.rateLimiter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RateLimitFilter implements Filter {

    private final IpRateLimiter ipRateLimiter;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        String ip = servletRequest.getRemoteAddr();
        String endpoint = ((HttpServletRequest) servletRequest).getRequestURI();

        RateLimiter rateLimiter = ipRateLimiter.getRateLimiter(ip, endpoint);

        if (rateLimiter.acquirePermission()) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
        }
    }
}
