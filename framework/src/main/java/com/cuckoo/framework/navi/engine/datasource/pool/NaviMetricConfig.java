package com.cuckoo.framework.navi.engine.datasource.pool;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NaviMetricConfig extends NaviPoolConfig {

    private long slowQuery = 200;
    private long bigQuery = 20000;

}
