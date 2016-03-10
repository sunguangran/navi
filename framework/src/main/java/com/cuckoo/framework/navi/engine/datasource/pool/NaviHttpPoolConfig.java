package com.cuckoo.framework.navi.engine.datasource.pool;

import org.springframework.beans.factory.InitializingBean;

/**
 * 多线程http client配置
 *
 */
public class NaviHttpPoolConfig extends NaviPoolConfig implements
    InitializingBean {

    private int maxPerRoute = 20; // 每个路由最大连接数
    private int timeToLive = 3000; // 链接保持时间
    private int retryTimes = 1; // 重试
    private String charset = null;
    private String proxy = null;// 代理10.10.0.1:9999
    private String userAgent = null;

    public int getMaxPerRoute() {
        return maxPerRoute;
    }

    public void setMaxPerRoute(int maxPerRoute) {
        this.maxPerRoute = maxPerRoute;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub

    }

}
