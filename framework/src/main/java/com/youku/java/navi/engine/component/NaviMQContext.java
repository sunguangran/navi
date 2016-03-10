package com.youku.java.navi.engine.component;

public class NaviMQContext {

    /**
     * 启动线程数
     */
    private int threadRate = 3;

    /**
     * 每次消费个数
     */
    private int consumeRate = 1;

    /**
     * 线程阻塞时间，单位毫秒
     */
    private int blockTime = 10;

    /**
     * 发生异常后线程睡眠时间，单位毫秒
     */
    private int exceptionSleepTime = 1000;

    /**
     * MQ实现类型，默认1，Redis实现
     */
    private MessageQueueType mqType = MessageQueueType.REDIS;

    public int getConsumeRate() {
        return consumeRate;
    }

    public void setConsumeRate(int consumeRate) {
        if (consumeRate > 0) {
            this.consumeRate = consumeRate;
        }
    }

    public int getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(int blockTime) {
        if (blockTime > 0) {
            this.blockTime = blockTime;
        }
    }

    public void setMqType(int mqType) {
        if (mqType >= 0 && mqType < MessageQueueType.values().length) {
            this.mqType = MessageQueueType.values()[mqType];
        }
    }

    public int getMqType() {
        return mqType.ordinal();
    }

    public MessageQueueType getMqEnumType() {
        return mqType;
    }

    public int getThreadRate() {
        return threadRate;
    }

    public void setThreadRate(int threadRate) {
        if (threadRate > 0) {
            this.threadRate = threadRate;
        }
    }

    public void setExceptionSleepTime(int exceptionSleepTime) {
        this.exceptionSleepTime = exceptionSleepTime;
    }

    public int getExceptionSleepTime() {
        return exceptionSleepTime;
    }

    public enum MessageQueueType {
        REDIS, MUTIREDIS, OLDMUTIREDIS, ZOOKEEPER
    }
}
