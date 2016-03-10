package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.engine.core.INaviZookeeper;
import com.youku.java.navi.engine.core.IZookeeperEventHander;
import com.youku.java.navi.engine.datasource.driver.NaviZooKeeperDriver;
import com.youku.java.navi.utils.AlibabaJsonSerializer;
import com.youku.java.navi.utils.NaviUtil;
import com.youku.java.navi.common.exception.NaviSystemException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.List;

public class NaviZookeeperService extends AbstractNaviDataService implements
    INaviZookeeper {

    private AlibabaJsonSerializer jsonSerializer = new AlibabaJsonSerializer();

    public void setCSEventHandler(IZookeeperEventHander eventHandler) {
        getZooKeeperDriver().registerEventHandler(eventHandler);
    }

    private NaviZooKeeperDriver getZooKeeperDriver() {
        return (NaviZooKeeperDriver) getDataSource().getHandle();
    }

    public void deletePath(String path) throws NaviSystemException {
        try {
            getZooKeeperDriver().delete(path);
        } catch (InterruptedException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (KeeperException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    public <T> void setData(String path, T data) throws NaviSystemException {
        try {
            getZooKeeperDriver().setData(path,
                jsonSerializer.getJSONBytes(data));
        } catch (SerializationException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (KeeperException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (InterruptedException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }

    }

    public <T> T getData(String path, Class<T> classNm)
        throws NaviSystemException {
        return getData(path, classNm, false);
    }

    public List<String> getChildren(String path) throws NaviSystemException {
        return getChildren(path, false);
    }

    public boolean exists(String path) throws NaviSystemException {
        return exists(path, false);
    }

    public boolean exists(String path, boolean watch)
        throws NaviSystemException {
        try {
            return getZooKeeperDriver().exists(path, watch) != null;
        } catch (KeeperException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (InterruptedException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    public boolean exists(String path, Watcher watcher)
        throws NaviSystemException {
        try {
            return getZooKeeperDriver().exists(path, watcher) != null;
        } catch (KeeperException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (InterruptedException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    public <T> T getData(String path, Class<T> classNm, boolean watch)
        throws NaviSystemException {
        try {
            return jsonSerializer.getObjectFromBytes(getZooKeeperDriver()
                .getData(path, watch, null), classNm);
        } catch (SerializationException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (KeeperException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (InterruptedException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    public <T> T getData(String path, Class<T> classNm, Watcher watch)
        throws NaviSystemException {
        try {
            return jsonSerializer.getObjectFromBytes(getZooKeeperDriver()
                .getData(path, watch, null), classNm);
        } catch (SerializationException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (KeeperException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (InterruptedException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    public List<String> getChildren(String path, boolean watch)
        throws NaviSystemException {
        try {
            return getZooKeeperDriver().getChildren(path, watch);
        } catch (KeeperException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (InterruptedException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    public List<String> getChildren(String path, Watcher watcher)
        throws NaviSystemException {
        try {
            return getZooKeeperDriver().getChildren(path, watcher);
        } catch (KeeperException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (InterruptedException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    public <T> String createEphemeralSequentialPath(String path, T initData) {
        try {
            return getZooKeeperDriver().create(path,
                jsonSerializer.getJSONBytes(initData), Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        } catch (KeeperException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (InterruptedException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    public <T> String createPersistentPath(String path, T initData) {
        try {
            return getZooKeeperDriver().create(path,
                jsonSerializer.getJSONBytes(initData), Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        } catch (KeeperException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (InterruptedException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    public <T> String createPersistentSequentialPath(String path, T initData) {
        try {
            return getZooKeeperDriver().create(path,
                jsonSerializer.getJSONBytes(initData), Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT_SEQUENTIAL);
        } catch (KeeperException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (InterruptedException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    public <T> String createEphemeralPath(String path, T initData) {
        try {
            return getZooKeeperDriver().create(path,
                jsonSerializer.getJSONBytes(initData), Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL);
        } catch (KeeperException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (InterruptedException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

}
