package com.dubic.ratelimiter.endpoints;

import com.dubic.ratelimiter.endpoints.data.Payload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ratelimited")
public class EndpointController {
    private final EndpointService endpointService;

    public EndpointController(EndpointService endpointService) {
        this.endpointService = endpointService;
    }

    @PostMapping
    public ResponseEntity<?> sendNotification(@RequestBody Payload payload){
        this.endpointService.processPayload(payload);
        return ResponseEntity.ok("Successful");
    }
}
