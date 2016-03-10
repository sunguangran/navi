package com.cuckoo.framework.navi.engine.datasource.driver;

import com.cuckoo.framework.navi.common.ServerUrlUtil.ServerUrl;
import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;

public class NaviHiveJdbcDriver extends NaviJdbcDriver {

    public NaviHiveJdbcDriver(ServerUrl server, String auth,
                              NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
    }

    @Override
    void loadDriver() throws ClassNotFoundException {
        Class.forName("org.apache.hadoop.hive.jdbc.HiveDriver");
    }

}
