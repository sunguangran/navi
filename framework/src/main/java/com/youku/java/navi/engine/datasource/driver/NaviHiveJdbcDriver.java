package com.youku.java.navi.engine.datasource.driver;

import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import com.youku.java.navi.common.ServerUrlUtil;

public class NaviHiveJdbcDriver extends NaviJdbcDriver {

    public NaviHiveJdbcDriver(ServerUrlUtil.ServerUrl server, String auth,
                              NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
    }

    @Override
    void loadDriver() throws ClassNotFoundException {
        Class.forName("org.apache.hadoop.hive.jdbc.HiveDriver");
    }

}
