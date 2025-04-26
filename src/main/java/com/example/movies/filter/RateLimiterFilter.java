package com.example.movies.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiterFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private final Map<String, ClientRequestInfo> clientRequestMap = new ConcurrentHashMap<>();

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (httpRequest.getRequestURI().startsWith("/ghibli")) {
            String clientIdentifier = getClientIdentifier(httpRequest);

            if (isRateLimitExceeded(clientIdentifier)) {
                httpResponse.setStatus(429); // HTTP 429 Too Many Requests
                httpResponse.getWriter().write("Too Many Requests - Rate limit exceeded. Try again later.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String getClientIdentifier(HttpServletRequest request) {
        return request.getRemoteAddr(); // Use IP address for public APIs
    }

    private boolean isRateLimitExceeded(String clientIdentifier) {
        ClientRequestInfo requestInfo = clientRequestMap.computeIfAbsent(clientIdentifier, k -> new ClientRequestInfo());
        synchronized (requestInfo) {
            long currentTime = Instant.now().getEpochSecond();
            if (currentTime - requestInfo.timestamp > 60) {
                requestInfo.timestamp = currentTime;
                requestInfo.requestCount.set(0);
            }
            if (requestInfo.requestCount.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
                return true;
            }
        }
        return false;
    }

    private static class ClientRequestInfo {
        long timestamp = Instant.now().getEpochSecond();
        AtomicInteger requestCount = new AtomicInteger(0);
    }
}
