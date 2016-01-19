package com.cuckoo.framework.navi.engine.component;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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
    private MQType mqType = MQType.REDIS;

    public void setConsumeRate(int consumeRate) {
        if (consumeRate > 0) {
            this.consumeRate = consumeRate;
        }
    }

    public void setBlockTime(int blockTime) {
        if (blockTime > 0) {
            this.blockTime = blockTime;
        }
    }

    public void setMqType(int mqType) {
        if (mqType >= 0 && mqType < MQType.values().length) {
            this.mqType = MQType.values()[mqType];
        }
    }

    public int getMqType() {
        return mqType.ordinal();
    }

    public MQType getMqEnumType() {
        return mqType;
    }

    public void setThreadRate(int threadRate) {
        if (threadRate > 0) {
            this.threadRate = threadRate;
        }
    }

    public enum MQType {
        REDIS, MUTIREDIS, OLDMUTIREDIS, ZOOKEEPER
    }
}
