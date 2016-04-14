package com.youku.java.navi.boot;

import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public class NaviAsyncMain extends ANaviMain {

    public static void main(String[] args) {
        log.info("starting aync process!");
        NaviAsyncMain mainClass = new NaviAsyncMain();
        try {
            mainClass.doMain(mainClass.parseServerConfig(args));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    @Override
    public String getStartClass(Properties serverConfig) {
        return NaviDefine.ASYNC_SERVERCLASS;
    }

    @Override
    public Properties parseServerConfig(String[] args) {
        Properties serverConfig = new Properties();
        if (NaviDefine.NAVI_HOME == null) {
            log.info("the current mode is dev!");
            serverConfig.put(NaviDefine.MODE, NaviDefine.WORK_MODE.DEV.toString());
        } else {
            serverConfig = parseConfig(NaviDefine.NAVI_CONF_PATH);
            log.info("the current mode is " + serverConfig.getProperty(NaviDefine.MODE));
        }

        return serverConfig;
    }

    @Override
    public String getConfPath() {
        return null;
    }
}
