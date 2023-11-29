package com.dubic.ratelimiter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

@Configuration
public class RedisConfig implements DisposableBean {
    Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${redis.host}")
    private String host;
    @Value("${redis.port}")
    private int port;
    private JedisPool jedisPool;

    @Bean
    public JedisPool jedisPool() {
        JedisPool jedisPool = new JedisPool(this.host, this.port);
        logger.info("Redis connected... :");
        this.jedisPool = jedisPool;
        return jedisPool;
    }

    @Override
    public void destroy() throws Exception {
        logger.info("Destroying jedis");
        this.jedisPool.close();
    }

}
