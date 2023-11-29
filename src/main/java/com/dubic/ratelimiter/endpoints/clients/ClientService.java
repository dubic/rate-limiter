package com.dubic.ratelimiter.endpoints.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;

@Service
public class ClientService {
    Logger logger = LoggerFactory.getLogger(ClientService.class);
    private final Map<String, Client> clientsMap = new HashMap<>();
    private final JedisPool jedisPool;

    public ClientService(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public Client getClientInfo(String clientId) {
        Client client = clientsMap.get(clientId);
        if (client == null) {
            throw new RuntimeException("No client with id :" + clientId);
        }
        return client;
    }

    public void createClient(Client client) {
        this.clientsMap.put(client.clientId(), client);
        logger.info("Created: {}", client);
    }

    public void dropClients() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("month_key_clientA", "month_key_clientB", "month_key_clientC",
                    "burst_key_sys", "burst_key_clientA", "burst_key_clientB", "burst_key_clientC");
        }
        this.clientsMap.clear();
    }
}
