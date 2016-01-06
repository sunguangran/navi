package com.cuckoo.framework.navi.engine.core;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

/**
 * 队列消费策略
 *
 * @param <T>
 */
public interface INaviMQConsumeStrategy<T> extends InitializingBean, DisposableBean {

    /**
     * 消费操作
     *
     * @param list
     */
    void consume(List<T> list);

    /**
     * 队列中元素类型
     *
     * @return
     */
    Class<T> getClassNM();
}
