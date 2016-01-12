package com.cuckoo.framework.navi.engine.datasource;

import com.cuckoo.framework.navi.common.NaviError;
import com.cuckoo.framework.navi.common.exception.NaviSystemException;
import com.cuckoo.framework.navi.engine.core.INaviDriver;
import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;
import com.cuckoo.framework.navi.server.ServerConfigure;
import com.cuckoo.framework.navi.utils.ServerUrlUtil;
import com.cuckoo.framework.navi.utils.ServerUrlUtil.ServerUrl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 按照不同host:port，创建对应的驱动实例，适用于自身带有连接池且具有重连接<br>
 * 以及异常处理的驱动实例, 可以随机访问获得驱动实例，使用效果一致
 */
@Slf4j
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
            ServerUrl serverAddr = new ServerUrl(
                (workMode == null && ServerConfigure.isDeployEnv()) || (workMode != null && workMode.equals("deploy")) ? getDeployConnectString() : getOfflineConnectString()
            );

            INaviDriver naviDriver = (INaviDriver) BeanUtils.instantiateClass(
                handleClassNm.getDeclaredConstructor(ServerUrl.class, String.class, NaviPoolConfig.class), serverAddr, getAuth(), this.poolConfig
            );

            poolDrivers.add(naviDriver);

        } else {
            List<ServerUrl> serverUrls = ServerUrlUtil.getServerUrl(
                (workMode == null && ServerConfigure.isDeployEnv()) || (workMode != null && workMode.equals("deploy")) ? getDeployConnectString() : getOfflineConnectString()
            );

            for (ServerUrl serverUrl : serverUrls) {
                INaviDriver naviDriver = (INaviDriver) BeanUtils.instantiateClass(
                    handleClassNm.getDeclaredConstructor(
                        ServerUrl.class, String.class, NaviPoolConfig.class
                    ), serverUrl, getAuth(), this.poolConfig
                );
                poolDrivers.add(naviDriver);
            }
        }
    }

    @Override
    public INaviDriver getHandle() throws NaviSystemException {
        // 安全模式
        if (isSafeMode()) {
            throw new NaviSystemException("the datasource " + getNamespace() + " is the safemode.", NaviError.SYSERROR.code());
        }

        int index = random.nextInt(poolDrivers.size());
        return poolDrivers.get(index);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isBlank(getOfflineConnectString()) || StringUtils.isBlank(getDeployConnectString())) {
            throw new NaviSystemException("invalid server address!", NaviError.SYSERROR.code());
        } else if (StringUtils.isBlank(getDriverClass())) {
            throw new NaviSystemException("invalid driverClass!", NaviError.SYSERROR.code());
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
                for (Iterator<INaviDriver> it = poolDrivers.iterator(); it.hasNext();) {
                    System.out.println(it.next().isAlive());
                    if (!it.next().isAlive()) {
                        it.remove();
                        offlinePoolDrivers.add(it.next());
                    }
                }
                for (Iterator<INaviDriver> it = offlinePoolDrivers.iterator(); it.hasNext();) {
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
                log.error("{}", e.getMessage());
            }
        }

        poolDrivers.clear();
        poolDrivers = null;
        //exeCheckPool.shutdown();
    }
}
