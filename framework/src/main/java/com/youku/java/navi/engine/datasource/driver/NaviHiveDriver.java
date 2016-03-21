package com.youku.java.navi.engine.datasource.driver;

import com.youku.java.navi.common.ServerUrlUtil;
import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;

public class NaviHiveDriver extends NaviJdbcDriver {

    public NaviHiveDriver(ServerUrlUtil.ServerUrl server, String auth, NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
    }

    @Override
    void loadDriver() throws ClassNotFoundException {
        Class.forName("org.apache.hive.jdbc.HiveDriver");
    }

}
