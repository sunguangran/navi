package com.cuckoo.framework.navi.engine.datasource.driver;

import com.cuckoo.framework.navi.common.NaviError;
import com.cuckoo.framework.navi.common.ServerAddress;
import com.cuckoo.framework.navi.common.exception.NaviRuntimeException;
import com.cuckoo.framework.navi.common.exception.NaviSystemException;
import com.cuckoo.framework.navi.engine.core.INaviDriver;
import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.concurrent.atomic.AtomicBoolean;

@Setter
@Getter
public abstract class ANaviDriver implements INaviDriver {

    private ServerAddress server;
    private String auth;
    private GenericObjectPool<INaviDriver> pool;
    private AtomicBoolean close = new AtomicBoolean();
    private AtomicBoolean broken = new AtomicBoolean();
    private NaviPoolConfig poolConfig;

    public ANaviDriver(ServerAddress server, String auth) {
        this(server, auth, null);
    }

    public ANaviDriver(ServerAddress server, String auth, NaviPoolConfig poolConfig) {
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
                throw new NaviRuntimeException(NaviError.SYSERROR.code(), "can not return the resource to the pool", e);
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
