package com.cuckoo.framework.navi.boot;

import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public abstract class ANaviTCPMain extends ANaviMain {

    @Override
    public Properties parseServerConfig(String[] args) {
        Properties serverCfg;
        if (NaviProps.NAVI_HOME == null) {
            log.warn("the config file is invalid!So will use default config.");
            
            serverCfg = new Properties();
            serverCfg.setProperty(NaviProps.PORT, NaviProps.DEFAULT_PORT);
            serverCfg.setProperty(NaviProps.SERVER, NaviProps.DEFAULT_SERVER);
            serverCfg.setProperty(NaviProps.CHUNK_AGGR_SIZE, NaviProps.DEFAULT_CHUNK_SIZE);
        } else {
            serverCfg = parseConfig(getConfPath());
        }

        return serverCfg;
    }

}
