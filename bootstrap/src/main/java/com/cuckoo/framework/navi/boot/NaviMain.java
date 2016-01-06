package com.cuckoo.framework.navi.boot;

import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * 支持HTTP服务入口
 */
@Slf4j
public class NaviMain extends ANaviMain {

    @Override
    public String getStartClass(Properties sysprop) {
        String protocol = sysprop.getProperty(NaviProps.PROTOCOL);
        switch (NaviProps.WORK_PROTOCOL.toProtocol(protocol)) {
            case TCP:
                return NaviProps.SERVERCLASS_TCP;
            case UDP:
                return NaviProps.SERVERCLASS_UDP;
            default:
                return NaviProps.SERVERCLASS;
        }
    }

    @Override
    public String getConfPath() {
        return NaviProps.NAVI_CONF_PATH;
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
