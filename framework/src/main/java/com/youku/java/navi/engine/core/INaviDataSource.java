package com.youku.java.navi.engine.core;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public interface INaviDataSource extends DisposableBean, InitializingBean {

    void setNamespace(String namespace);

    String getNamespace();

    void setOfflineConnectString(String offlineConnectString);

    void setDeployConnectString(String deployConnectString);

    void setType(String type);

    void setWorkMode(String workMode);

    void setSlowQuery(long slowQuery);

    long getSlowQuery();

    void setAuth(String auth);

    /**
     * 记录日志方法
     */
    void log();

    /**
     * 监控方法
     */
    void monitor();

    /**
     * 获取数据源句柄
     */
    INaviDriver getHandle();

}
