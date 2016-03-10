package com.cuckoo.framework.navi.engine.core;

import org.apache.zookeeper.WatchedEvent;

/**
 * 节点状态变更事件处理类
 *
 */
public interface IZookeeperEventHander {

    /**
     * 当zookeeper节点变化时激活逻辑
     *
     * @param e
     */
    public void processForNode(WatchedEvent e);

    /**
     * 注册监听(实现此方法后,框架可以在断线重连后帮助重新注册监听)
     */
    public void registWatch();
}
