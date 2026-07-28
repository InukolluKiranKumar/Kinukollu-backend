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

    private static class Bucket {
        AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart = System.currentTimeMillis();
    }

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final long WINDOW_MS = 60_000;
    private static final int MAX_REQUESTS_PER_WINDOW = 15;
    private static final long MAX_BUCKET_AGE_MS = 5 * 60_000; // evict entries idle for 5+ min

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean shouldLimit = path.equals("/api/auth/signup")
                || path.equals("/api/auth/login")
                || path.matches("/api/cases/\\d+/ask");

        if (!shouldLimit) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        long now = System.currentTimeMillis();

        // Periodic cleanup: remove stale buckets so the map never grows unbounded
        if (buckets.size() > 1000) {
            buckets.entrySet().removeIf(e -> now - e.getValue().windowStart > MAX_BUCKET_AGE_MS);
        }

        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> new Bucket());

        synchronized (bucket) {
            if (now - bucket.windowStart > WINDOW_MS) {
                bucket.windowStart = now;
                bucket.count.set(0);
            }

            int count = bucket.count.incrementAndGet();

            if (count > MAX_REQUESTS_PER_WINDOW) {
                response.setStatus(429);
                response.setContentType("text/plain");
                response.getWriter().write("Too many requests. Please wait a moment and try again.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
