package com.cuckoo.framework.navi.server;

import com.cuckoo.framework.navi.boot.INaviServer;
import org.apache.commons.lang.StringUtils;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class ANaviServer implements INaviServer {

    public final int setupServer(Properties serverCfg) {
        if (preStartServer(serverCfg)) {
            String zkopen = ServerConfigure.get("zk.open");
            if (!StringUtils.isEmpty(zkopen) && zkopen.equalsIgnoreCase("true")) {
                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new NaviRegisterTask(getServerType()), 0, 1, TimeUnit.MINUTES);
            }

            int statusCode = startServer();
            if (statusCode != SUCCESS) {
                return statusCode;
            }

            postStartServer();
        }
        return SUCCESS;
    }

    protected abstract NaviServerType getServerType();

    protected abstract boolean preStartServer(Properties serverCfg);

    protected abstract int startServer();

    protected abstract void postStartServer();


}
