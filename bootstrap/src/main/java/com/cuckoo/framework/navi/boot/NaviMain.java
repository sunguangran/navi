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
        String protocol = sysprop.getProperty(NaviDefine.PROTOCOL);
        switch (NaviDefine.WORK_PROTOCOL.toProtocol(protocol)) {
            case HTTP:
                return NaviDefine.SERVERCLASS;
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
            if (NaviDefine.NAVI_HOME == null) {
                System.out.println("NAVI_HOME not defined");
                System.exit(1);
            }

            NaviMain mainClass = new NaviMain();

            Properties props = mainClass.parseServerConfig(args);
            if (props == null) {
                log.error("config parse failed.");
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
