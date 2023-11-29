package com.dubic.ratelimiter.ratelimiting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Queue;

@Service
public class RateLimiterQueue {
    Logger logger = LoggerFactory.getLogger(RateLimiterQueue.class);
    private final Queue<String> queue = new ArrayDeque<>();

    public void queueRequest(String request) {
        queue.add(request);
        logger.info("Queued({}) to be processed in 1min : {}", queue.size(), request);
    }
}
