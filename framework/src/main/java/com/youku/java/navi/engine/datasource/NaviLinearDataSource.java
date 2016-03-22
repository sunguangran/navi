package com.youku.java.navi.engine.datasource;

import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.ServerUrlUtil;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.core.INaviDriver;
import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import com.youku.java.navi.server.ServerConfigure;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 按照不同host:port，创建对应的驱动实例，适用于自身带有连接池且具有重连接<br>
 * 以及异常处理的驱动实例, 可以随机访问获得驱动实例，使用效果一致
 */
public class NaviLinearDataSource extends DefaultNaviDataSource {

    private List<INaviDriver> poolDrivers;

    @Override
    protected void initConnPool() throws Exception {
        if (isSafeMode()) {
            return;
        }

        poolDrivers = new ArrayList<>();
        random = new Random();
        Class<?> handleClassNm = getContextClassLoader().loadClass(getDriverClass());

        if (!isSplitHosts()) {
            ServerUrlUtil.ServerUrl serverAddr = new ServerUrlUtil.ServerUrl(
                (workMode == null && ServerConfigure.isDeployEnv()) || (workMode != null && workMode.equals("deploy")) ? getDeployConnectString() : getOfflineConnectString()
            );

            INaviDriver naviDriver = (INaviDriver) BeanUtils.instantiateClass(
                handleClassNm.getDeclaredConstructor(ServerUrlUtil.ServerUrl.class, String.class, NaviPoolConfig.class), serverAddr, getAuth(), this.poolConfig
            );

            poolDrivers.add(naviDriver);
        } else {
            List<ServerUrlUtil.ServerUrl> serverUrls = ServerUrlUtil.getServerUrl(
                (workMode == null && ServerConfigure.isDeployEnv()) || (workMode != null && workMode.equals("deploy")) ? getDeployConnectString() : getOfflineConnectString()
            );

            for (ServerUrlUtil.ServerUrl serverUrl : serverUrls) {
                INaviDriver naviDriver = (INaviDriver) BeanUtils.instantiateClass(handleClassNm.getDeclaredConstructor(
                    ServerUrlUtil.ServerUrl.class, String.class, NaviPoolConfig.class), serverUrl, getAuth(), this.poolConfig
                );

                poolDrivers.add(naviDriver);
            }
        }
    }

    @Override
    public INaviDriver getHandle() throws NaviSystemException {
        if (isSafeMode()) {
            // 安全模式
            throw new NaviSystemException(
                "the dataSource " + getNamespace() + " is safemode.", NaviError.SYSERROR
            );
        }

        int index = random.nextInt(poolDrivers.size());
        return poolDrivers.get(index);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isBlank(getOfflineConnectString()) || StringUtils.isBlank(getDeployConnectString())) {
            throw new NaviSystemException("invalid server address!", NaviError.SYSERROR);
        } else if (StringUtils.isBlank(getDriverClass())) {
            throw new NaviSystemException("invalid driverClass!", NaviError.SYSERROR);
        }

        initConnPool();
        //exeCheckPool = Executors.newSingleThreadScheduledExecutor();
        //exeCheckPool.scheduleAtFixedRate(new CheckPool(), 0, SLEEPTIME, TimeUnit.SECONDS);
    }

    /*
        public class CheckPool extends TimerTask {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                for (Iterator<INaviDriver> it = poolDrivers.iterator(); it
                        .hasNext();) {
                    System.out.println(it.next().isAlive());
                    if (!it.next().isAlive()) {
                        it.remove();
                        offlinePoolDrivers.add(it.next());
                    }
                }
                for (Iterator<INaviDriver> it = offlinePoolDrivers.iterator(); it
                        .hasNext();) {
                    System.out.println(it.next().isAlive());
                    if (it.next().isAlive()) {
                        it.remove();
                        poolDrivers.add(it.next());
                    }
                }
            }
        }
    */
    @Override
    public void destroy() {
        for (INaviDriver driver : poolDrivers) {
            try {
                driver.destroy();
            } catch (Exception e) {
                // throw new NaviSystemException(e.getMessage(),
                // NaviError.SYSERROR, e);
            }
        }

        poolDrivers.clear();
        poolDrivers = null;
        //exeCheckPool.shutdown();
    }
}
