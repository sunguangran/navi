package com.youku.java.navi.engine.datasource;

import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.ServerUrlUtil;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.core.INaviDriver;
import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import com.youku.java.navi.server.ServerConfigure;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Setter
@Getter
@Slf4j
public class DefaultNaviDataSource extends AbstractNaviDataSource implements ApplicationContextAware {

    protected Random random;
    private boolean safeMode;
    private List<GenericObjectPool<INaviDriver>> pools;
    protected NaviPoolConfig poolConfig = new NaviPoolConfig();
    private ClassLoader contextClassLoader;
    private boolean splitHosts;
    private String driverClass;

    public INaviDriver getHandle() {
        try {
            int index = random.nextInt(pools.size());
            GenericObjectPool<INaviDriver> pool = pools.get(index);
            INaviDriver handle = pool.borrowObject();
            handle.setPool(pool);

            return handle;
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    protected void initConnPool() throws Exception {
        if (safeMode) {
            return;
        }

        pools = new ArrayList<>();
        random = new Random();
        Class<?> handleClassNm = getContextClassLoader().loadClass(driverClass);

        if (!splitHosts) {
            ServerUrlUtil.ServerUrl serverAddr = new ServerUrlUtil.ServerUrl(
                ServerConfigure.isDeployEnv() ? getDeployConnectString() : getOfflineConnectString()
            );

            GenericObjectPool<INaviDriver> pool = new GenericObjectPool<>(
                new NaviPoolableObjectFactory(handleClassNm, serverAddr, auth), poolConfig
            );

            pools.add(pool);
        } else {
            List<ServerUrlUtil.ServerUrl> serverUrls = ServerUrlUtil.getServerUrl(
                ServerConfigure.isDeployEnv() ? getDeployConnectString() : getOfflineConnectString()
            );

            for (ServerUrlUtil.ServerUrl serverUrl : serverUrls) {
                GenericObjectPool<INaviDriver> pool = new GenericObjectPool<>(
                    new NaviPoolableObjectFactory(handleClassNm, serverUrl, auth), poolConfig
                );

                pools.add(pool);
            }
        }
    }

    public void log() {

    }

    public void monitor() {

    }

    public void destroy() throws Exception {
        for (GenericObjectPool<INaviDriver> pool : pools) {
            try {
                if (pool != null) {
                    pool.close();
                }
            } catch (Exception e) {
                log.error("{}", e.getMessage());
            }
        }
    }

    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isBlank(getOfflineConnectString()) || StringUtils.isBlank(getDeployConnectString())) {
            throw new NaviSystemException("invalid server address!",
                NaviError.SYSERROR);
        } else if (StringUtils.isBlank(driverClass)) {
            throw new NaviSystemException("invalid driverClass!",
                NaviError.SYSERROR);
        }
        initConnPool();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.contextClassLoader = applicationContext.getClassLoader();
    }

    private class NaviPoolableObjectFactory extends BasePooledObjectFactory<INaviDriver> {

        private Class<?> handleClass;
        private String auth;
        private ServerUrlUtil.ServerUrl server;

        private NaviPoolableObjectFactory(Class<?> handleClass, ServerUrlUtil.ServerUrl server, String auth) {
            this.handleClass = handleClass;
            this.auth = auth;
            this.server = server;
        }

        @Override
        public INaviDriver create() throws Exception {
            return (INaviDriver) BeanUtils.instantiateClass(
                handleClass.getDeclaredConstructor(ServerUrlUtil.ServerUrl.class, String.class, NaviPoolConfig.class), this.server, this.auth, poolConfig
            );
        }

        @Override
        public void destroyObject(PooledObject<INaviDriver> pooled) throws Exception {
            pooled.getObject().destroy();
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
}
