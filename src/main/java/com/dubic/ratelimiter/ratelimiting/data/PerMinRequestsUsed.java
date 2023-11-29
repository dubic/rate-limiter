package com.dubic.ratelimiter.ratelimiting.data;

public record PerMinRequestsUsed(long usedReqPerMin, long usedSystemReqPerMin) {
}
