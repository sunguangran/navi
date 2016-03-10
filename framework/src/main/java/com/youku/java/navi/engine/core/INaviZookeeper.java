package com.youku.java.navi.engine.core;

import org.apache.zookeeper.Watcher;

import java.util.List;


public interface INaviZookeeper extends IBaseDataService {

    /**
     * 事件同步器
     *
     * @param eventHandler
     */
    public void setCSEventHandler(IZookeeperEventHander eventHandler);

    /**
     * 创建持久路径
     *
     * @param <T>
     * @param path
     * @param initData
     *     初始数据
     * @return 创建的实际路径
     */
    public <T> String createPersistentPath(String path, T initData);

    /**
     * 创建持久自增路径
     *
     * @param <T>
     * @param path
     * @param initData
     * @return
     */
    public <T> String createPersistentSequentialPath(String path, T initData);

    /**
     * 创建临时路径
     *
     * @param <T>
     * @param path
     * @param initData
     * @return
     */
    public <T> String createEphemeralPath(String path, T initData);

    /**
     * 创建临时路径
     *
     * @param <T>
     * @param path
     * @param initData
     * @return
     */
    public <T> String createEphemeralSequentialPath(String path, T initData);

    /**
     * 判断$path是否存在
     *
     * @param path
     * @return
     */
    public boolean exists(String path);

    /**
     * 判断$path是否存在
     *
     * @param path
     * @param watch是否对该路径放置监听器
     * @return
     */
    public boolean exists(String path, boolean watch);

    /**
     * 判断$path是否存在
     *
     * @param path
     * @param watcher事件监听器
     * @return
     */
    public boolean exists(String path, Watcher watcher);

    /**
     * 删除$path
     *
     * @param path
     */
    public void deletePath(String path);

    /**
     * 对指定$path设置数据
     *
     * @param <T>
     * @param path
     * @param data
     */
    public <T> void setData(String path, T data);

    /**
     * 获取指定$path数据
     *
     * @param <T>
     * @param path
     * @param classNm
     *     data class name
     * @return
     */
    public <T> T getData(String path, Class<T> classNm);

    /**
     * 获取指定$path数据
     *
     * @param <T>
     * @param path
     * @param classNm
     * @param watch是否对该路径放置监听器
     * @return
     */
    public <T> T getData(String path, Class<T> classNm, boolean watch);

    /**
     * 获取指定$path数据
     *
     * @param <T>
     * @param path
     * @param classNm
     * @param watcher
     *     事件监听器
     * @return
     */
    public <T> T getData(String path, Class<T> classNm, Watcher watcher);

    /**
     * 获取指定$path子目录
     *
     * @param path
     * @return
     */
    public List<String> getChildren(String path);

    /**
     * 获取指定$path子目录
     *
     * @param path
     * @param watch是否监控该路径
     * @return
     */
    public List<String> getChildren(String path, boolean watch);

    /**
     * 获取指定$path子目录
     *
     * @param path
     * @param watcher
     *     事件监听器
     * @return
     */
    public List<String> getChildren(String path, Watcher watcher);

}
