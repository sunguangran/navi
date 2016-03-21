package com.youku.java.navi.engine.datasource;

import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.core.INaviDriver;
import com.youku.java.navi.engine.core.IUDPClientDataSource;
import com.youku.java.navi.engine.datasource.pool.NaviUDPClientPoolConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class NaviUDPClietnDataSource implements IUDPClientDataSource, ApplicationContextAware {
    private GenericObjectPool<INaviDriver> pool;
    private ClassLoader contextClassLoader;
    private String driverClass;
    private NaviUDPClientPoolConfig poolConfig;

    protected void initConnPool() throws Exception {
        Class<?> handleClassNm = getContextClassLoader().loadClass(driverClass);
        pool = new GenericObjectPool<>(
            new NaviPoolableObjectFactory(handleClassNm), poolConfig);
    }

    public INaviDriver getHandle() {
        INaviDriver handle = null;
        try {
            handle = pool.borrowObject();
            handle.setPool(pool);
            return handle;
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(),
                NaviError.SYSERROR, e);
        }

    }

    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isBlank(getDriverClass())) {
            throw new NaviSystemException("invalid driverClass!",
                NaviError.SYSERROR);
        }
        initConnPool();
    }

    public void destroy() {
        pool.close();
    }

    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException {
        this.contextClassLoader = applicationContext.getClassLoader();
    }

    public ClassLoader getContextClassLoader() {
        return contextClassLoader;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public NaviUDPClientPoolConfig getPoolConfig() {
        return poolConfig;
    }

    public void setPoolConfig(NaviUDPClientPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    protected class NaviPoolableObjectFactory extends BasePooledObjectFactory<INaviDriver> {

        private Class<?> handleClass;

        public NaviPoolableObjectFactory(Class<?> handleClass) {
            this.handleClass = handleClass;
        }

        @Override
        public INaviDriver create() throws Exception {
            return (INaviDriver) BeanUtils.instantiateClass(handleClass
                .getDeclaredConstructor());
        }

        @Override
        public void destroyObject(PooledObject<INaviDriver> p) throws Exception {
            p.getObject().destroy();
        }

        @Override
        public boolean validateObject(PooledObject<INaviDriver> p) {
            try {
                return p.getObject().isAlive();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public PooledObject<INaviDriver> wrap(INaviDriver obj) {
            return new DefaultPooledObject<>(obj);
        }
    }

    public void setNamespace(String namespace) {
        // TODO Auto-generated method stub

    }

    public String getNamespace() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setOfflineConnectString(String offlineConnectString) {
        // TODO Auto-generated method stub

    }

    public void setDeployConnectString(String deployConnectString) {
        // TODO Auto-generated method stub

    }

    public void setType(String type) {
        // TODO Auto-generated method stub

    }

    public void setWorkMode(String workMode) {
        // TODO Auto-generated method stub

    }

    public void setSlowQuery(long slowQuery) {
        // TODO Auto-generated method stub

    }

    public long getSlowQuery() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void setAuth(String auth) {
        // TODO Auto-generated method stub

    }

    public void log() {
        // TODO Auto-generated method stub

    }

    public void monitor() {
        // TODO Auto-generated method stub

    }
}
