package com.cuckoo.framework.navi.engine.datasource.pool;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;

/**
 * 多线程http client配置
 */
@Setter
@Getter
public class NaviHttpPoolConfig extends NaviPoolConfig implements InitializingBean {

    private int maxPerRoute = 20; // 每个路由最大连接数
    private int timeToLive = 3000; // 链接保持时间
    private int retryTimes = 1; // 重试
    private String charset = null;
    private String proxy = null;// 代理10.10.0.1:9999
    private String userAgent = null;

    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub
    }

}
