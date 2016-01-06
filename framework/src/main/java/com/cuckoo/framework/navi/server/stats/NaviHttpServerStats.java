package com.cuckoo.framework.navi.server.stats;

import java.util.concurrent.atomic.AtomicLong;

public class NaviHttpServerStats implements NaviHttpServerStatsMBean {

    private AtomicLong connCount = new AtomicLong();
    private AtomicLong readCount = new AtomicLong();
    private AtomicLong writeCount = new AtomicLong();
    private long lastGetReadRateTime;
    private long lastGetWriteRateTime;
    private long lastReadCount;
    private long lastWriteCount;
    private long writeByte;
    private long readByte;
    private long lastWriteByte;
    private long lastReadByte;
    private long lastWriteThroghputTime;
    private long lastReadThroghputTime;

    public void incrConnCount() {
        connCount.incrementAndGet();
    }

    public long getConnCount() {
        return connCount.get();
    }

    public void decrConnCount() {
        connCount.decrementAndGet();
    }

    public void incrReadCount() {
        readCount.incrementAndGet();
    }

    public int getReadCountPerSecd() {
        long currentTimeMillis = System.currentTimeMillis();
        long currReadCount = readCount.get();
        int tmpt = (int) ((currReadCount - lastReadCount) * 1000 / (currentTimeMillis - lastGetReadRateTime));
        lastReadCount = currReadCount;
        lastGetReadRateTime = currentTimeMillis;
        return tmpt;
    }

    public void incrWriteCount() {
        writeCount.incrementAndGet();
    }

    public int getWriteCountPerSecd() {
        long currentTimeMillis = System.currentTimeMillis();
        long currWriteCount = writeCount.get();
        int tmpt = (int) ((currWriteCount - lastWriteCount) * 1000 / (currentTimeMillis - lastGetWriteRateTime));
        lastWriteCount = currWriteCount;
        lastGetWriteRateTime = currentTimeMillis;
        return tmpt;
    }

    public double getResponseRate() {
        long currReadCount = readCount.get();
        if (currReadCount == 0) {
            return 0;
        }
        return ((double) writeCount.get()) / currReadCount;
    }

    public int getReadThroughput() {
        long currentTimeMillis = System.currentTimeMillis();
        int tmpt = (int) ((readByte - lastReadByte) * 1000 / (currentTimeMillis - lastReadThroghputTime));
        lastReadByte = readByte;
        lastReadThroghputTime = currentTimeMillis;
        return tmpt >> 10;
    }

    public int getWriteThroughput() {
        long currentTimeMillis = System.currentTimeMillis();
        int tmpt = (int) ((writeByte - lastWriteByte) * 1000 / (currentTimeMillis - lastWriteThroghputTime));
        lastWriteByte = writeByte;
        lastWriteThroghputTime = currentTimeMillis;
        return tmpt >> 10;
    }

    public void setWriteByte(long writeByte) {
        this.writeByte = writeByte;
    }

    public void setReadByte(long readByte) {
        this.readByte = readByte;
    }

    public long getWriteByte() {
        return writeByte;
    }

    public long getReadByte() {
        return readByte;
    }

}
