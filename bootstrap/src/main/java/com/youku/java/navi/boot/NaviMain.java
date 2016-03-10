package com.youku.java.navi.boot;

import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * 支持HTTP服务入口
 */
@Slf4j
public class NaviMain extends ANaviMain {

    @Override
    public String getStartClass(Properties sysprop) {
        String protocol = sysprop.getProperty(NaviDefine.PROTOCOL);
        switch (NaviDefine.WORK_PROTOCOL.toProtocol(protocol)) {
            case TCP:
                return NaviDefine.SERVERCLASS_TCP;
            case UDP:
                return NaviDefine.SERVERCLASS_UDP;
            default:
                return NaviDefine.SERVERCLASS;
        }
    }

    @Override
    public String getConfPath() {
        return NaviDefine.NAVI_CONF_PATH;
    }

    public static void main(String[] args) {
        try {
            NaviMain mainClass = new NaviMain();

            Properties props = mainClass.parseServerConfig(args);
            if (props == null) {
                System.exit(1);
            }

            mainClass.doMain(props);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            System.exit(1);
        }
    }
}
