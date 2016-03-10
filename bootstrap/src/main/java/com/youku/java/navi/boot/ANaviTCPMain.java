package com.youku.java.navi.boot;

import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public abstract class ANaviTCPMain extends ANaviMain {

    @Override
    public Properties parseServerConfig(String[] args) {
        Properties serverCfg;
        if (NaviDefine.NAVI_HOME == null) {
            log.warn("the config file is invalid!So will use default config.");
            
            serverCfg = new Properties();
            serverCfg.setProperty(NaviDefine.PORT, NaviDefine.DEFAULT_PORT);
            serverCfg.setProperty(NaviDefine.SERVER, NaviDefine.DEFAULT_SERVER);
            serverCfg.setProperty(NaviDefine.CHUNK_AGGR_SIZE, NaviDefine.DEFAULT_CHUNK_SIZE);
        } else {
            serverCfg = parseConfig(getConfPath());
        }

        return serverCfg;
    }

}
