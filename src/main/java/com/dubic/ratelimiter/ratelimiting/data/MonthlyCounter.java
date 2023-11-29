package com.dubic.ratelimiter.ratelimiting.data;

public record MonthlyCounter(long usedReqPerMonth, boolean monthlyRequestLimitExceeded) {
}
