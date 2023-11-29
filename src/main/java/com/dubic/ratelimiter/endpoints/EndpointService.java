package com.dubic.ratelimiter.endpoints;

import com.dubic.ratelimiter.endpoints.data.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EndpointService {

    Logger logger = LoggerFactory.getLogger(EndpointService.class);

    public void processPayload(Payload payload) {
        logger.info("processed payload [{}]", payload);
    }
}
