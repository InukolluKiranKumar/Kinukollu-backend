package com.kinukollu.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // IP -> request count in current window
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> windowStart = new ConcurrentHashMap<>();

    private static final long WINDOW_MS = 60_000; // 1 minute window
    private static final int MAX_REQUESTS_PER_WINDOW = 15; // generous but bounded

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only rate-limit sensitive/costly endpoints
        boolean shouldLimit = path.equals("/api/auth/signup")
                || path.equals("/api/auth/login")
                || path.matches("/api/cases/\\d+/ask");

        if (!shouldLimit) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        long now = System.currentTimeMillis();

        windowStart.putIfAbsent(clientIp, now);
        requestCounts.putIfAbsent(clientIp, new AtomicInteger(0));

        if (now - windowStart.get(clientIp) > WINDOW_MS) {
            windowStart.put(clientIp, now);
            requestCounts.get(clientIp).set(0);
        }

        int count = requestCounts.get(clientIp).incrementAndGet();

        if (count > MAX_REQUESTS_PER_WINDOW) {
            response.setStatus(429);
            response.setContentType("text/plain");
            response.getWriter().write("Too many requests. Please wait a moment and try again.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
