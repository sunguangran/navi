package com.youku.java.navi.engine.datasource.pool;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.json.JSONObject;

import java.util.HashMap;

@Slf4j
public class NaviHBasePoolConfig extends NaviPoolConfig {
    private HashMap<String, String> keyMap;
    private Configuration config;

    public Configuration getConfig() {
        return config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    /**
     * 使用连接信息进行连接设置
     *
     * @param connectString
     */
    public void setConfig(String connectString) {
        try {
            if (config == null) {
                config = HBaseConfiguration.create();
            }
            JSONObject json = new JSONObject(connectString);
            for (Object key : json.keySet()) {
                if (keyMap.containsKey((String) key)) {
                    String name = keyMap.get((String) key);
                    config.set(name, json.getString((String) key));
                } else {
                    config.set((String) key, json.getString((String) key));
                }
            }

//			config.set("delete-connection",
//					json.has("delete-connection") ? json.getString("delete-connection") : "true");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public NaviHBasePoolConfig() {
        keyMap = new HashMap<String, String>();
//		keyMap.put("hbase.zookeeper.quorum", "mycluster");
//		conf.set("hbase.zookeeper.quorum", "hadoop1");
//		keyMap.put("zk", "hbase.zookeeper.quorum");
//		keyMap.put("FS", "fs.defaultFS");
    }

    public NaviHBasePoolConfig(String connectString) {
        this();
        this.setConfig(connectString);
        this.setConnectTimeout(this.getConnectTimeout());
    }

    @Override
    public void setConnectTimeout(int connectTimeout) {
        super.setConnectTimeout(connectTimeout);
        if (config == null) {
            config = HBaseConfiguration.create();
        }
//		config.set("hbase.client.operation.timeout", String.valueOf(connectTimeout));
    }

    @Override
    public int getConnectTimeout() {
        int timeout = super.getConnectTimeout();
        try {
            if (config != null) {
                String ctimeout = config.get("hbase.client.operation.timeout");
                if (ctimeout != null) {
                    int t_real = Integer.parseInt(ctimeout);
                    if (t_real != timeout) {
                        //防止有人直接修改config对象
                        super.setConnectTimeout(t_real);
                        timeout = t_real;
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return timeout;
    }
}
