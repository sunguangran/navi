package com.cuckoo.framework.navi.engine.datasource.driver;

import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;
import com.cuckoo.framework.navi.common.ServerAddress;

public class NaviHiveDriver extends NaviJdbcDriver {

    public NaviHiveDriver(ServerAddress server, String auth,
                          NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
    }

    @Override
    void loadDriver() throws ClassNotFoundException {
        Class.forName("org.apache.hive.jdbc.HiveDriver");
    }

}
