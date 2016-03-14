package com.youku.java.navi.engine.datasource.driver;

import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.ServerUrlUtil;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.core.IZookeeperEventHander;
import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class NaviZooKeeperDriver extends AbstractNaviDriver {

    private ZooKeeper zooKeeper;
    private AtomicBoolean zooConn = new AtomicBoolean();
    private NaviWatcher watcher = new NaviWatcher();
    private final List<ACL> DEFAULT_ACL = Ids.OPEN_ACL_UNSAFE;
    private final CreateMode DEFAULT_CREATE_MODE = CreateMode.PERSISTENT;
    private List<IZookeeperEventHander> eventHandlers;

    public NaviZooKeeperDriver(ServerUrlUtil.ServerUrl server, String auth, NaviPoolConfig poolConfig) throws IOException {
        super(server, auth, poolConfig);
        zooKeeper = new ZooKeeper(server.getUrl(), poolConfig.getConnectTimeout(), watcher);
        eventHandlers = new ArrayList<IZookeeperEventHander>();
    }

    public void destroy() throws NaviSystemException {
        try {
            if (isAlive()) {
                zooKeeper.close();
                zooConn.set(false);
                zooKeeper = null;
                watcher = null;
                eventHandlers.clear();
                eventHandlers = null;
                log.info("zookeeper instance is destoried!");
            }
        } catch (InterruptedException e) {
            throw new NaviSystemException(e.getMessage(),
                NaviError.SYSERROR, e);
        }
    }

    public boolean isAlive() throws NaviSystemException {
        return zooKeeper.getState().isAlive();
    }

    private void handleWatchExpireEvent() throws IOException {
        try {
            if (isAlive()) {
                zooKeeper.close();
                zooConn.set(false);
            }
        } catch (InterruptedException e) {
            throw new NaviSystemException(e.getMessage(),
                NaviError.SYSERROR, e);
        }
        zooKeeper = new ZooKeeper(getServer().getUrl(), getPoolConfig()
            .getConnectTimeout(), watcher);
    }

    public Stat exists(String path, boolean watch) throws KeeperException,
        InterruptedException {
        return zooKeeper.exists(path, watch);
    }

    public Stat exists(String path, Watcher watcher) throws KeeperException,
        InterruptedException {
        return zooKeeper.exists(path, watcher);
    }

    public String create(String path, byte data[]) throws KeeperException,
        InterruptedException {
        return create(path, data, DEFAULT_ACL, DEFAULT_CREATE_MODE);
    }

    public String create(final String path, byte data[], List<ACL> acl,
                         CreateMode createMode) throws KeeperException, InterruptedException {
        return zooKeeper.create(path, data, acl, createMode);
    }

    public void delete(final String path, int version)
        throws InterruptedException, KeeperException {
        zooKeeper.delete(path, version);
    }

    public void delete(String path) throws InterruptedException,
        KeeperException {
        delete(path, -1);
    }

    public Stat setData(final String path, byte data[], int version)
        throws KeeperException, InterruptedException {
        return zooKeeper.setData(path, data, version);
    }

    public Stat setData(final String path, byte data[]) throws KeeperException,
        InterruptedException {
        return setData(path, data, -1);
    }

    public byte[] getData(final String path, Watcher watcher, Stat stat)
        throws KeeperException, InterruptedException {
        return zooKeeper.getData(path, watcher, stat);
    }

    public byte[] getData(String path, boolean watch, Stat stat)
        throws KeeperException, InterruptedException {
        return zooKeeper.getData(path, watch, stat);
    }

    public List<String> getChildren(final String path, Watcher watcher)
        throws KeeperException, InterruptedException {
        return zooKeeper.getChildren(path, watcher);
    }

    public List<String> getChildren(String path, boolean watch)
        throws KeeperException, InterruptedException {
        return zooKeeper.getChildren(path, watch);
    }

    public void validateConn() throws ConnectionLossException,
        InterruptedException {
        if (!zooConn.get()) {
            synchronized (zooConn) {
                if (!zooConn.get()) {
                    zooConn.wait(5000);
                }
            }
            if (!zooConn.get()) {
                throw new KeeperException.ConnectionLossException();
            }
        }
    }

    public void registerEventHandler(IZookeeperEventHander eventHandler) {
        this.eventHandlers.add(eventHandler);
    }

    private class NaviWatcher implements Watcher {

        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.None) {
                switch (event.getState()) {
                    case SyncConnected:
                        log.debug("the client has connected to the server!");
                        synchronized (zooConn) {
                            zooConn.set(true);
                            zooConn.notifyAll();
                        }
                        //重新注册监听
                        registWatch();
                        break;
                    case Disconnected:
                        log.debug("the client is disconnected to the server!");
                        zooConn.set(false);
                        break;
                    case Expired:
                        log.debug("the client session is expired for the server!then will create new zookeeper instance!");
                        try {
                            handleWatchExpireEvent();
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                        break;
                }
            }
            processEventType(event);
        }

        private void processEventType(WatchedEvent event) {
            synchronized (this) {
                for (IZookeeperEventHander eventHandler : eventHandlers) {
                    eventHandler.processForNode(event);
                }
            }
        }

        private void registWatch() {
            synchronized (this) {
                for (IZookeeperEventHander eventHandler : eventHandlers) {
                    eventHandler.registWatch();
                }
            }
        }
    }

    public boolean open() {
        return false;
    }

}
