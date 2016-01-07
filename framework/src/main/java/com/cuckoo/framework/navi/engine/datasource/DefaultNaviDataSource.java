package com.cuckoo.framework.navi.engine.datasource;

import com.cuckoo.framework.navi.common.NaviError;
import com.cuckoo.framework.navi.common.exception.NaviSystemException;
import com.cuckoo.framework.navi.engine.core.INaviDriver;
import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;
import com.cuckoo.framework.navi.server.ServerConfigure;
import com.cuckoo.framework.navi.utils.ServerUrlUtil;
import com.cuckoo.framework.navi.utils.ServerUrlUtil.ServerUrl;
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
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR.code(), e);
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
            ServerUrl serverAddr = new ServerUrl(
                ServerConfigure.isDeployEnv() ? getDeployConnectString() : getOfflineConnectString()
            );

            GenericObjectPool<INaviDriver> pool = new GenericObjectPool<>(
                new NaviPoolableObjectFactory(handleClassNm, serverAddr, auth), poolConfig
            );

            pools.add(pool);
        } else {
            List<ServerUrl> serverUrls = ServerUrlUtil.getServerUrl(
                ServerConfigure.isDeployEnv() ? getDeployConnectString() : getOfflineConnectString()
            );

            for (ServerUrl serverUrl : serverUrls) {
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
                NaviError.SYSERROR.code());
        } else if (StringUtils.isBlank(driverClass)) {
            throw new NaviSystemException("invalid driverClass!",
                NaviError.SYSERROR.code());
        }
        initConnPool();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.contextClassLoader = applicationContext.getClassLoader();
    }

    private class NaviPoolableObjectFactory extends BasePooledObjectFactory<INaviDriver> {

        private Class<?> handleClass;
        private String auth;
        private ServerUrl server;

        private NaviPoolableObjectFactory(Class<?> handleClass, ServerUrl server, String auth) {
            this.handleClass = handleClass;
            this.auth = auth;
            this.server = server;
        }

        @Override
        public INaviDriver create() throws Exception {
            return (INaviDriver) BeanUtils.instantiateClass(
                handleClass.getDeclaredConstructor(ServerUrl.class, String.class, NaviPoolConfig.class), this.server, this.auth, poolConfig
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
