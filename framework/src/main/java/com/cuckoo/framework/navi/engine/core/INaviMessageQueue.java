package com.cuckoo.framework.navi.engine.core;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Navi队列接口
 */
public interface INaviMessageQueue {

    /**
     * 入队操作
     *
     * @param <T>
     * @param key
     * @param e
     * @return
     */
    <T> boolean offer(String key, T... e);

    /**
     * 出队操作，没有数据返回null
     *
     * @param <T>
     * @param key
     * @param classNm
     * @return
     */
    <T> T poll(String key, Class<T> classNm);

    /**
     * 出队操作，没有数据抛异常
     *
     * @param <T>
     * @param key
     * @param classNm
     * @return
     */
    <T> T remove(String key, Class<T> classNm);

    /**
     * 阻塞式出队操作
     *
     * @param <T>
     * @param key
     * @param timeout
     * @param unit
     * @param classNm
     * @return
     */
    <T> T poll(String key, long timeout, TimeUnit unit, Class<T> classNm);

    /**
     * 阻塞式出队操作
     *
     * @param <T>
     * @param key
     * @param timeout
     * @param classNm
     * @return
     */
    <T> T poll(String key, long timeout, Class<T> classNm);

    /**
     * 阻塞式出队操作
     *
     * @param <T>
     * @param key
     * @param classNm
     * @return
     */
    <T> T take(String key, Class<T> classNm);

    /**
     * 查看队头元素，不存在则返回null
     *
     * @param <T>
     * @param key
     * @param classNm
     * @return
     */
    <T> T peek(String key, Class<T> classNm);

    /**
     * 查看对头元素，不存在则抛异常
     *
     * @param <T>
     * @param key
     * @param classNm
     * @return
     */
    <T> T element(String key, Class<T> classNm);

    /**
     * 批量出队
     *
     * @param <T>
     * @param key
     * @param c
     *     数据存储的容器
     * @param maxElements
     * @param classNm
     * @return 返回的数量
     */
    <T> int drainTo(String key, Collection<T> c, int maxElements, Class<T> classNm);

    /**
     * 队列长度
     *
     * @param key
     * @return
     */
    long size(String key);

    /**
     * 队列是否为空
     *
     * @param key
     * @return
     */
    boolean isEmpty(String key);

}