/*
 * MIT License
 *
 * Copyright (c) 2025 Kudzeri
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
