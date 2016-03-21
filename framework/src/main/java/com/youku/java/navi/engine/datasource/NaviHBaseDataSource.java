package com.youku.java.navi.engine.datasource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youku.java.navi.engine.datasource.pool.NaviHBasePoolConfig;
import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import com.youku.java.navi.server.ServerConfigure;
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
        JSONObject json;
        if (ServerConfigure.isDeployEnv()) {
            json = JSON.parseObject(this.deployConnectString);
        } else {
            json = JSON.parseObject(this.offlineConnectString);
        }
        
        NaviPoolConfig poolConfig = new NaviHBasePoolConfig(json.toString());
        this.setConfig(poolConfig);
        super.initConnPool();
    }
}
