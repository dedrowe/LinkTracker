package backend.academy.scrapper.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@AllArgsConstructor
public class IpRateLimitFilter implements Filter {

    private final IpRateLimiter ipRateLimiter;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException {
        String ip = servletRequest.getRemoteAddr();
        String endpoint = ((HttpServletRequest) servletRequest).getRequestURI();

        boolean allowed = ipRateLimiter.tryExecute(ip, endpoint, () -> {
            try {
                filterChain.doFilter(servletRequest, servletResponse);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        if (!allowed) {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
        }
    }
}
