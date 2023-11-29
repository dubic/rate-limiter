package com.dubic.ratelimiter.ratelimiting.data;

public record Counter(long usedReqPerMin, long usedSystemReqPerMin, boolean perMinuteRequestLimitExceeded) {
}
