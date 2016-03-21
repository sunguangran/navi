package com.youku.java.navi.engine.datasource.driver;

import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.ServerUrlUtil;
import com.youku.java.navi.common.exception.NaviRuntimeException;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.core.INaviDriver;
import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractNaviDriver implements INaviDriver {

    @Setter @Getter
    private ServerUrlUtil.ServerUrl server;

    @Setter @Getter
    private String auth;

    @Setter @Getter
    private GenericObjectPool<INaviDriver> pool;
    private AtomicBoolean close = new AtomicBoolean();
    private AtomicBoolean broken = new AtomicBoolean();

    @Setter @Getter
    private NaviPoolConfig poolConfig;

    public AbstractNaviDriver(ServerUrlUtil.ServerUrl server, String auth) {
        this(server, auth, null);
    }

    public AbstractNaviDriver(ServerUrlUtil.ServerUrl server, String auth, NaviPoolConfig poolConfig) {
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
                    NaviError.SYSERROR, e);
            }
        } finally {
            close.set(true);
        }
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

    public void afterPropertiesSet() throws Exception {

    }

    public Object getDriver() {
        return null;
    }
}
