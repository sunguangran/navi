package com.youku.java.navi.server;

import com.youku.java.navi.boot.NaviDefine;
import com.youku.java.navi.engine.component.NaviMQConsumeController;
import com.youku.java.navi.server.module.INaviModuleContext;
import com.youku.java.navi.server.module.NaviAsyncModuleContextFactory;
import com.youku.java.navi.server.module.NaviDevFrameWorkContext;
import com.youku.java.navi.server.module.NaviFrameWorkContext;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Properties;

public class NaviAsyncServer extends ANaviServer {

    private NaviMQConsumeController controller;
    private Logger logger = Logger.getLogger(NaviAsyncServer.class);

    public void stopServer() {
        if (controller != null) {
            try {
                controller.destroy();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    protected boolean preStartServer(Properties serverCfg) {
        ServerConfigure.setServerCfg(serverCfg);
        //加载模块配置文件
        try {
            NaviAsyncModuleContextFactory.getInstance().loadNaviAsyncModuleContext();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    protected int startServer() {
        try {
            INaviModuleContext context;
            if (ServerConfigure.isDevEnv() && !new File(NaviDefine.NAVI_CONF_PATH).exists()) {
                //本地开发环境
                context = new NaviDevFrameWorkContext("async").initModule();
            } else {
                context = new NaviFrameWorkContext("async").initModule();
            }

            controller = (NaviMQConsumeController) context.getBean("controller");
            controller.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected void postStartServer() {
        //开启配置文件修改检查
        NaviAsyncModuleContextFactory.getInstance().startCheckModuleProccess();
    }

    @Override
    protected NaviServerType getServerType() {
        return NaviServerType.AsyncServer;
    }

}
