package com.youku.java.navi.engine.datasource.pool;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class NaviPoolConfig extends GenericObjectPoolConfig {

    /**
     * 连接超时
     */
    private int connectTimeout = 2000;

    /**
     * 最大阻塞线程数
     */
    private int maxBlockingThread;

    /**
     * I/O读写超时
     */
    private int socketTimeout = 3000;

    public NaviPoolConfig() {
        setMaxWait(100);
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getMaxBlockingThread() {
        return maxBlockingThread;
    }

    public void setMaxBlockingThread(int maxBlockingThread) {
        this.maxBlockingThread = maxBlockingThread;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getMaxActive() {
        return getMaxTotal();
    }

    public void setMaxActive(int maxActive) {
        setMaxTotal(maxActive);
    }

    public long getMaxWait() {
        return getMaxWaitMillis();
    }

    public void setMaxWait(long maxWait) {
        setMaxWaitMillis(maxWait);
    }

}
