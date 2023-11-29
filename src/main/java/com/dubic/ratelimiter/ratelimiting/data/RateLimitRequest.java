package com.dubic.ratelimiter.ratelimiting.data;

import org.springframework.util.StringUtils;

public record RateLimitRequest(String clientId) {
    public RateLimitRequest {
        if(!StringUtils.hasLength(clientId)){
            throw new RuntimeException("Client ID ['clientId'] must be supplied in the header");
        }
    }
}
