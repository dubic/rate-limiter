package com.dubic.ratelimiter.ratelimiting;

import com.dubic.ratelimiter.endpoints.clients.Client;
import com.dubic.ratelimiter.endpoints.clients.ClientService;
import com.dubic.ratelimiter.ratelimiting.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.args.ExpiryOption;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
public class RateLimitService {
    Logger logger = LoggerFactory.getLogger(RateLimitService.class);
    private final ClientService clientService;
    private final JedisPool jedisPool;

    @Value("${max.req.per.min}")
    private long maxReqPerMin;

    public RateLimitService(ClientService clientService, JedisPool jedis) {
        this.clientService = clientService;
        this.jedisPool = jedis;
    }

    public RateLimitResponse checkLimits(RateLimitRequest rateLimitRequest) {
        logger.info("checking rate limits : [{}]", rateLimitRequest.clientId());
        Client clientInfo = this.clientService.getClientInfo(rateLimitRequest.clientId());

        Long usedReqPerMonth = this.incrementAndExpireMonthLimit(clientInfo);

        if (this.monthlyRequestLimitExceeded(clientInfo, usedReqPerMonth)) {
            return new RateLimitResponse(new MonthlyCounter(usedReqPerMonth, true),
                    new Counter(0, 0, false),
                    clientInfo);
        }

        PerMinRequestsUsed perMinRequestsUsed = this.incrementAndExpirePerMinLimits(clientInfo);
        if (this.perMinuteRequestLimitExceeded(clientInfo, perMinRequestsUsed)) {
            return new RateLimitResponse(new MonthlyCounter(usedReqPerMonth, false),
                    new Counter(perMinRequestsUsed.usedReqPerMin(), perMinRequestsUsed.usedSystemReqPerMin(),
                            true),
                    clientInfo);
        }

        return new RateLimitResponse(new MonthlyCounter(usedReqPerMonth, false),
                new Counter(perMinRequestsUsed.usedReqPerMin(), perMinRequestsUsed.usedSystemReqPerMin(),
                        false),
                clientInfo);
    }

    private boolean perMinuteRequestLimitExceeded(Client clientInfo, PerMinRequestsUsed limits) {
        if (limits.usedReqPerMin() > clientInfo.minuteMax()) {
            logger.error("Request per minute limit exceeded for : {}, max: {}", clientInfo.name(),
                    clientInfo.minuteMax());
            return true;
        } else if (limits.usedSystemReqPerMin() > maxReqPerMin) {
            logger.error("System max request per minute limit exceeded : {}", maxReqPerMin);
            return true;
        }
        return false;
    }

    private boolean monthlyRequestLimitExceeded(Client clientInfo, Long usedReqPerMonth) {
        if (usedReqPerMonth > clientInfo.monthlyMax()) {
            logger.error("Monthly request limit exceeded for : {}, max: {}", clientInfo.name(),
                    clientInfo.monthlyMax());
            return true;
        }
        return false;
    }

    private Long incrementAndExpireMonthLimit(Client clientInfo) {
        final String monthlyMaxKey = clientInfo.getMonthlyMaxKey();
        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            Response<Long> monthUsedResponse = transaction.incr(monthlyMaxKey);
            transaction.expire(monthlyMaxKey, secondsUntilMonthEnd(), ExpiryOption.NX);
            transaction.exec();
            return monthUsedResponse.get();
        }
    }

    private PerMinRequestsUsed incrementAndExpirePerMinLimits(Client clientInfo) {
        final String burstMaxKey = clientInfo.getBurstMaxKey();
        final String burstMaxKeySystem = clientInfo.getBurstMaxKeySystem();

        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();

            //burst request rate limit counter for client
            Response<Long> burstMaxResponse = transaction.incr(burstMaxKey);
            transaction.expire(burstMaxKey, secondsUntilOneMinute(), ExpiryOption.NX);
            //burst request rate limit counter for system
            Response<Long> burstMaxSysResponse = transaction.incr(burstMaxKeySystem);
            transaction.expire(burstMaxKeySystem, secondsUntilOneMinute(), ExpiryOption.NX);

            transaction.exec();
            return new PerMinRequestsUsed(burstMaxResponse.get(), burstMaxSysResponse.get());
        }
    }

    private long secondsUntilOneMinute() {
        return 60L;
    }

    private Long secondsUntilMonthEnd() {
        // Get the current date
        LocalDate currentDate = LocalDate.now(ZoneId.of("Africa/Lagos"));
        // Calculate the last day of the current month
        LocalDate lastDayOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
        // Set the time to 23:59:59.999 (last millisecond of the day)
        LocalDateTime endOfDay = lastDayOfMonth.atTime(LocalTime.MAX);
        // Calculate the number of seconds until the end of the month
        return LocalDateTime.now(ZoneId.of("Africa/Lagos")).until(endOfDay, ChronoUnit.SECONDS);
    }
}
