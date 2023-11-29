package com.dubic.ratelimiter.endpoints.clients;

public record Client(String clientId, String name, Long monthlyMax, Long minuteMax) {

    public String getMonthlyMaxKey() {
        return "month_key_".concat(this.clientId);
    }

    public String getBurstMaxKey() {
        return "burst_key_".concat(this.clientId);
    }

    public String getBurstMaxKeySystem() {
        return "burst_key_sys";
    }
}
