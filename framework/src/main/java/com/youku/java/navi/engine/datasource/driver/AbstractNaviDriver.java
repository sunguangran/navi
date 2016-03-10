package com.youku.java.navi.engine.datasource.driver;

import com.youku.java.navi.common.exception.NaviRuntimeException;
import com.youku.java.navi.engine.core.INaviDriver;
import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import com.youku.java.navi.common.NAVIERROR;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.common.ServerUrlUtil;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractNaviDriver implements INaviDriver {

    private ServerUrlUtil.ServerUrl server;
    private String auth;
    private GenericObjectPool<INaviDriver> pool;
    private AtomicBoolean close = new AtomicBoolean();
    private AtomicBoolean broken = new AtomicBoolean();
    private NaviPoolConfig poolConfig;

    public AbstractNaviDriver(ServerUrlUtil.ServerUrl server, String auth) {
        this(server, auth, null);
    }

    public AbstractNaviDriver(ServerUrlUtil.ServerUrl server, String auth,
                              NaviPoolConfig poolConfig) {
        this.server = server;
        this.auth = auth;
        this.poolConfig = poolConfig;
    }

    public void close() throws NaviSystemException {
        if (pool == null) {
            return;
        }
        try {
            if (broken.get()) {
                pool.invalidateObject(this);
            } else {
                pool.returnObject(this);
            }
        } catch (Exception ex) {
            try {
                pool.invalidateObject(this);
            } catch (Exception e) {
                throw new NaviRuntimeException(
                    "Could not return the resource to the pool",
                    NAVIERROR.SYSERROR.code(), e);
            }
        } finally {
            close.set(true);
        }
    }

    public ServerUrlUtil.ServerUrl getServer() {
        return server;
    }

    public void setServer(ServerUrlUtil.ServerUrl server) {
        this.server = server;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public GenericObjectPool<INaviDriver> getPool() {
        return pool;
    }

    public void setPool(GenericObjectPool<INaviDriver> pool) {
        this.pool = pool;
    }

    public boolean isClose() {
        return close.get();
    }

    public void setClose(boolean close) {
        this.close.set(close);
    }

    public boolean isBroken() {
        return broken.get();
    }

    public void setBroken(boolean broken) {
        this.broken.set(broken);
    }

    public NaviPoolConfig getPoolConfig() {
        return poolConfig;
    }

    public void setPoolConfig(NaviPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    public void afterPropertiesSet() throws Exception {

    }

    public Object getDriver() {
        return null;
    }
}
