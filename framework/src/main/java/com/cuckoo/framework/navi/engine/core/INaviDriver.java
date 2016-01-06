package com.cuckoo.framework.navi.engine.core;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.InitializingBean;

public interface INaviDriver extends InitializingBean {

    /**
     * 销毁驱动
     */
    void destroy();

    /**
     * 判断本地驱动引擎连接状态
     *
     * @return 连接状态
     */
    boolean isAlive();

    /**
     * 连接驱动引擎
     *
     * @return 是否成功
     */
    boolean open();

    /**
     * 将驱动返回连接池
     */
    void close();

    /**
     * 获取驱动
     */
    Object getDriver();

    /**
     * 设置所属连接池
     */
    void setPool(GenericObjectPool<INaviDriver> pool);

}
