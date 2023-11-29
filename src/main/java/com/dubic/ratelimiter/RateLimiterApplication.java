package com.dubic.ratelimiter;

import com.dubic.ratelimiter.endpoints.clients.Client;
import com.dubic.ratelimiter.endpoints.clients.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RateLimiterApplication implements CommandLineRunner {
	@Autowired
	private ClientService clientService;

	public static void main(String[] args) {
		SpringApplication.run(RateLimiterApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		this.clientService.createClient(new Client("clientA", "Client A", 10L, 10L));
		this.clientService.createClient(new Client("clientB", "Client B", 30L, 5L));
		this.clientService.createClient(new Client("clientC", "Client C", 20L, 3L));
	}

}
