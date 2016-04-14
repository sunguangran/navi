package com.youku.java.navi.boot;

import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public class NaviDaemonMain extends ANaviMain {

    public static void main(String[] args) {
        log.info("starting daemon process!");
        if (args.length < 1) {
            log.error("the parameter is invalid");
            return;
        }

        try {
            NaviDaemonMain mainClass = new NaviDaemonMain();
            mainClass.doMain(mainClass.parseServerConfig(args));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public String getStartClass(Properties serverConfig) {
        return NaviDefine.DAEMON_SERVERCLASS;
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

        serverConfig.put(NaviDefine.DAEMON_MODULE_NAME, args[0]);
        if (args.length > 1) {
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
            serverConfig.put(NaviDefine.DAEMON_CLASS_ARGS, newArgs);
        }

        return serverConfig;
    }

    @Override
    public String getConfPath() {
        return null;
    }
}
