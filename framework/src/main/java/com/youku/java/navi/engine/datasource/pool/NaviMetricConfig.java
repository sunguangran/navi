package com.youku.java.navi.engine.datasource.pool;

public class NaviMetricConfig extends NaviPoolConfig {

    private long slowQuery = 200;

    private long bigQuery = 20000;

    public void setSlowQuery(long slowQuery) {
        this.slowQuery = slowQuery;
    }

    public long getSlowQuery() {
        return slowQuery;
    }

    public void setBigQuery(long bigQuery) {
        this.bigQuery = bigQuery;
    }

    public long getBigQuery() {
        return bigQuery;
    }

}
