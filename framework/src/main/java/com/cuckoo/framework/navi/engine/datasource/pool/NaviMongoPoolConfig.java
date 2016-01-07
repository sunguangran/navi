package com.cuckoo.framework.navi.engine.datasource.pool;

import com.mongodb.MongoOptions;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;

@Setter
@Getter
public class NaviMongoPoolConfig extends NaviPoolConfig implements InitializingBean {

    private MongoOptions options;
    private boolean autoConnectRetry = true;
    private int maxAutoConnectRetryTime;
    private boolean safe = true;
    private boolean slaveOk = false;
    private boolean socketKeepAlive = false;

    public MongoOptions getOptions() {
        return options;
    }

    public void setOptions(MongoOptions options) {
        this.options = options;
    }

    @SuppressWarnings("deprecation")
    public void afterPropertiesSet() throws Exception {

        options = new MongoOptions();
        /**
         * 设置获取连接池句柄的最大等待时间s
         */
        options.maxWaitTime = (int) getMaxWait();
        /**
         * 写安全设置，如果为true，驱动每次update后会发出一个getLastError命令来保证成功， If true the driver
         * will use a WriteConcern of WriteConcern.SAFE for all operations.
         */
        options.safe = isSafe();

        /**
         * 系统在发生连接错误时是否重试 ，默认为true
         */
        options.autoConnectRetry = isAutoConnectRetry();
        /**
         * 自动重连时间间隔
         */
        if (maxAutoConnectRetryTime != 0) {
            options.maxAutoConnectRetryTime = getMaxAutoConnectRetryTime();
        }

        /**
         * 设置最大线程阻塞数，乘数
         */
        if (getMaxBlockingThread() != 0) {
            options.threadsAllowedToBlockForConnectionMultiplier = getMaxBlockingThread();
        }
        /**
         * 设置socket通讯超时时间
         */
        if (getSocketTimeout() != 0) {
            options.socketTimeout = getSocketTimeout();
            System.setProperty("com.mongodb.updaterConnectTimeoutMS", String.valueOf(getSocketTimeout()));
        }
        /**
         * 设置socket连接超时时间
         */
        if (getConnectTimeout() != 0) {
            options.connectTimeout = getConnectTimeout();
            System.setProperty("com.mongodb.updaterSocketTimeoutMS", String.valueOf(getConnectTimeout()));
        }
        /**
         * 设置连接池最大连接数
         */
        if (getMaxActive() != 0) {
            options.connectionsPerHost = getMaxActive();
        }
        options.slaveOk = isSlaveOk();
        options.socketKeepAlive = isSocketKeepAlive();
    }
}
