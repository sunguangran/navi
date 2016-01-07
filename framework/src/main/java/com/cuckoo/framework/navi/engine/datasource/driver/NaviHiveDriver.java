package com.cuckoo.framework.navi.engine.datasource.driver;

import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;
import com.cuckoo.framework.navi.utils.ServerUrlUtil.ServerUrl;

public class NaviHiveDriver extends NaviJdbcDriver {

    public NaviHiveDriver(ServerUrl server, String auth,
                          NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
    }

    @Override
    void loadDriver() throws ClassNotFoundException {
        Class.forName("org.apache.hive.jdbc.HiveDriver");
    }

}
