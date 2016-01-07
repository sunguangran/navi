package com.cuckoo.framework.navi.engine.datasource;

import com.alibaba.fastjson.JSONObject;
import com.cuckoo.framework.navi.engine.datasource.pool.NaviHBasePoolConfig;
import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;
import com.cuckoo.framework.navi.server.ServerConfigure;
import org.apache.hadoop.conf.Configuration;

public class NaviHBaseDataSource extends NaviLinearDataSource {
    private String offlineConnectString;
    private String deployConnectString;
    private NaviPoolConfig poolConfig;

    public String getOfflineConnectString() {
        return offlineConnectString;
    }

    @Override
    public void setOfflineConnectString(String offlineConnectString) {
        super.setOfflineConnectString(offlineConnectString);
        this.offlineConnectString = offlineConnectString;
    }

    public String getDeployConnectString() {
        return deployConnectString;
    }

    @Override
    public void setDeployConnectString(String deployConnectString) {
        super.setDeployConnectString(deployConnectString);
        this.deployConnectString = deployConnectString;
    }

    public Configuration getConfig() {
        return ((NaviHBasePoolConfig) getPoolConfig()).getConfig();
    }

    public void setConfig(Configuration config) {
        ((NaviHBasePoolConfig) poolConfig).setConfig(config);
    }

    public void setConfig(NaviPoolConfig config) {
        super.setPoolConfig(config);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        JSONObject json = null;
        if (ServerConfigure.isDeployEnv()) {
            json = JSONObject.parseObject(this.deployConnectString);
        } else {
            json = JSONObject.parseObject(this.offlineConnectString);
        }
        NaviPoolConfig poolConfig = new NaviHBasePoolConfig(json.toString());
        this.setConfig(poolConfig);
        super.initConnPool();
    }
}
