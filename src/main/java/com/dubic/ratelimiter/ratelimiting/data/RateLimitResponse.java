package com.dubic.ratelimiter.ratelimiting.data;


import com.dubic.ratelimiter.endpoints.clients.Client;

public record RateLimitResponse(MonthlyCounter monthlyCounter, Counter counter, Client client) {
}
