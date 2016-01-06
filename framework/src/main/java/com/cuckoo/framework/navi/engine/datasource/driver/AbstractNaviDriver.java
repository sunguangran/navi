package com.cuckoo.framework.navi.engine.datasource.driver;

import com.cuckoo.framework.navi.common.NaviRuntimeException;
import com.cuckoo.framework.navi.engine.core.INaviDriver;
import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;
import com.cuckoo.framework.navi.common.NAVIERROR;
import com.cuckoo.framework.navi.common.NaviSystemException;
import com.cuckoo.framework.navi.common.ServerUrlUtil.ServerUrl;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractNaviDriver implements INaviDriver {

    private ServerUrl server;
    private String auth;
    private GenericObjectPool<INaviDriver> pool;
    private AtomicBoolean close = new AtomicBoolean();
    private AtomicBoolean broken = new AtomicBoolean();
    private NaviPoolConfig poolConfig;

    public AbstractNaviDriver(ServerUrl server, String auth) {
        this(server, auth, null);
    }

    public AbstractNaviDriver(ServerUrl server, String auth,
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

    public ServerUrl getServer() {
        return server;
    }

    public void setServer(ServerUrl server) {
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
