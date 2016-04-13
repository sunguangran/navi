package com.youku.java.navi.server;

import com.youku.java.navi.boot.NaviDefine;
import com.youku.java.navi.server.handler.NaviDaemonJobRunner;
import org.apache.log4j.Logger;

import java.util.*;

public class NaviDaemonServer extends ANaviServer {

    private Logger log = Logger.getLogger(NaviDaemonServer.class);

    @Override
    protected boolean preStartServer(Properties serverCfg) {
        ServerConfigure.setServerCfg(serverCfg);
        log.info("prepared for starting server successfully!");
        return true;
    }

    @Override
    protected int startServer() {
        String moduleNm = ServerConfigure.get(NaviDefine.DAEMON_MODULE_NAME);
        String[] classArgs = ServerConfigure.getClassArgs();
        if (classArgs.length == 0) {
            log.error("A job name is required!");
            return FAIL;
        }

        Set<String> opts = new HashSet<String>();
        List<String> params = new ArrayList<String>();
        String jobNm = classArgs[0];
        for (String arg : classArgs) {
            if (arg.startsWith("-")) {
                opts.add(arg);
            } else {
                params.add(arg);
            }
        }

        String[] parameters = params.toArray(new String[params.size()]);
        try {
            NaviDaemonJobRunner jonRunner = new NaviDaemonJobRunner();
            return jonRunner.start(moduleNm, jobNm, parameters, opts);
        } catch (Exception e) {
            log.error(e);
            return FAIL;
        }
    }

    @Override
    protected NaviServerType getServerType() {
        return NaviServerType.DaemonServer;
    }

    @Override
    protected void postStartServer() {

    }

    public void stopServer() {

    }

}
