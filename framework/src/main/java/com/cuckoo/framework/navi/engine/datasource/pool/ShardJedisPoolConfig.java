package com.cuckoo.framework.navi.engine.datasource.pool;

import java.util.Set;


public class ShardJedisPoolConfig extends NaviPoolConfig {


    private Set<String> deploySentinels;
    private Set<String> sentinels;
    private boolean readMaster = true;

    public void setSentinels(Set<String> sentinels) {
        this.sentinels = sentinels;
    }

    public Set<String> getSentinels() {
        return sentinels;
    }

    public Set<String> getDeploySentinels() {
        return deploySentinels;
    }

    public void setDeploySentinels(Set<String> deploySentinels) {
        this.deploySentinels = deploySentinels;
    }

    public boolean isReadMaster() {
        return readMaster;
    }

    public void setReadMaster(boolean readMaster) {
        this.readMaster = readMaster;
    }

}
