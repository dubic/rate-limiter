package com.dubic.ratelimiter.endpoints.data;

public class Payload {
    private String subject;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        return "Payload{" +
                "subject='" + subject + '\'' +
                '}';
    }
}
