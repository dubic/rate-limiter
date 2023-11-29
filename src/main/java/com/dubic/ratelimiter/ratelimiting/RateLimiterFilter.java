package com.dubic.ratelimiter.ratelimiting;

import com.dubic.ratelimiter.ratelimiting.data.RateLimitRequest;
import com.dubic.ratelimiter.ratelimiting.data.RateLimitResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class RateLimiterFilter extends OncePerRequestFilter {
    Logger logger = LoggerFactory.getLogger(RateLimiterFilter.class);
    private final RateLimitService limitService;
    private final RateLimiterQueue rateLimiterQueue;

    public RateLimiterFilter(RateLimitService limitService, RateLimiterQueue rateLimiterQueue) {
        this.limitService = limitService;
        this.rateLimiterQueue = rateLimiterQueue;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        RateLimitRequest rateLimitRequest = new RateLimitRequest(request.getHeader("clientId"));
        RateLimitResponse limitResp = this.limitService.checkLimits(rateLimitRequest);

        response.setHeader("X-rate-monthly-requests",
                String.valueOf(limitResp.monthlyCounter().usedReqPerMonth()));
        response.setHeader("X-rate-monthly-requests-remaining",
                String.valueOf(limitResp.client().monthlyMax() - limitResp.monthlyCounter().usedReqPerMonth()));

        //return 429 for requests per month exceeded
        if (limitResp.monthlyCounter().monthlyRequestLimitExceeded()) {
            sendResponse(response, "Monthly requests limit exceeded", 429);
            return;
        }
        //return 202 for requests per minute exceeded
        if (limitResp.counter().perMinuteRequestLimitExceeded()) {
            String body =
                    request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            this.rateLimiterQueue.queueRequest(body);
            sendResponse(response, "Too many requests", HttpServletResponse.SC_ACCEPTED);
            return;
        }
        filterChain.doFilter(request, response);

    }

    private void sendResponse(HttpServletResponse response, String msg, int code) throws IOException {
        response.setStatus(code);
        response.getWriter().write(msg);
        response.getWriter().flush();
        response.getWriter().close();
    }
}
