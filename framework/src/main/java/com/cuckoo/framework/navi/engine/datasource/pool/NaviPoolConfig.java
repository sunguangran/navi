package com.cuckoo.framework.navi.engine.datasource.pool;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

@Setter
@Getter
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
