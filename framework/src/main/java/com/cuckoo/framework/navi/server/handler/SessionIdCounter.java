package com.cuckoo.framework.navi.server.handler;

import java.util.concurrent.atomic.AtomicLong;

public class SessionIdCounter {

    private final static SessionIdCounter instance = new SessionIdCounter();
    private AtomicLong sessionId = new AtomicLong();


    public long incAndGet() {
        return sessionId.incrementAndGet();
    }

    public long decAndGet() {
        return sessionId.decrementAndGet();
    }

    public long getSessionId() {
        return sessionId.get();
    }

    public static SessionIdCounter getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return String.valueOf(sessionId.get());
    }
}
