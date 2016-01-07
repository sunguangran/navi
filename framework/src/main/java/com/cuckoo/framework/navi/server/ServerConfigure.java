package com.cuckoo.framework.navi.server;

import com.cuckoo.framework.navi.boot.NaviDefine;
import org.apache.commons.lang.StringUtils;

import java.util.Properties;

public final class ServerConfigure implements NaviDefine {

    private final static int DEFAULT_MODULE_LOAD_PERIOD = 30;// 30s
    private final static int DEFAULT_REQUEST_OUT = 0;// 0ms
    private final static int DEFAULT_CHANNEL_IDLETIME = 60;// 60s

    private static Properties serverCfg = new Properties();

    public static boolean isDeployEnv() {
        return WORK_MODE.DEPLOY == getWorkMode();
    }

    public static boolean isDaemonEnv() {
        return false;
    }

    public static String get(String name) {
        return serverCfg.getProperty(name);
    }

    public static WORK_MODE getWorkMode() {
        return WORK_MODE.toEnum(get(MODE));
    }

    public static String getPort() {
        return System.getProperty(PORT) != null ? System.getProperty(PORT) : get(PORT);
    }

    public static void setServerCfg(Properties serverCfg) {
        ServerConfigure.serverCfg = serverCfg;
    }

    public static boolean containsKey(String name) {
        return serverCfg.containsKey(name);
    }

    public static String getServer() {
        return get(SERVER);
    }

    public static int getChildChannelIdleTime() {
        try {
            return Integer.valueOf(get(CHILD_CHANNEL_IDLTIME));
        } catch (Exception e) {
            return DEFAULT_CHANNEL_IDLETIME;
        }
    }

    public static boolean isChannelClose() {
        if (StringUtils.isBlank(get(CHILD_CHANNEL_CLOSE))) {
            return true;
        }
        return Boolean.valueOf(get(CHILD_CHANNEL_CLOSE));
    }

    public static int getModuleLoadInterval() {

        return get(MODULE_LOAD_INTERVAL) == null ? DEFAULT_MODULE_LOAD_PERIOD : Integer.valueOf(get(MODULE_LOAD_INTERVAL));
    }

    public static int getRequestTimeout() {
        return get(REQUEST_TIMEOUT) == null ? DEFAULT_REQUEST_OUT : Integer.valueOf(get(REQUEST_TIMEOUT));
    }
}
