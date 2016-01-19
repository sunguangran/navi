package com.cuckoo.framework.navi.engine.datasource.driver;

import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;
import com.cuckoo.framework.navi.common.ServerAddress;

public class NaviHiveJdbcDriver extends NaviJdbcDriver {

    public NaviHiveJdbcDriver(ServerAddress server, String auth,
                              NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
    }

    @Override
    void loadDriver() throws ClassNotFoundException {
        Class.forName("org.apache.hadoop.hive.jdbc.HiveDriver");
    }

}
