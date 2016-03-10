package com.youku.java.navi.engine.redis;

import com.youku.java.navi.engine.datasource.pool.ShardJedisPoolConfig;
import com.youku.java.navi.server.ServerConfigure;
import redis.clients.util.ShardInfo;
import redis.clients.util.Sharded;

import java.util.Set;

public class MultiJedisPoolInfo extends ShardInfo<MultiJedisPool> {

    private ShardJedisPoolConfig poolConfig;
    private String master;

    public MultiJedisPoolInfo(String master, ShardJedisPoolConfig poolConfig) {
        super(Sharded.DEFAULT_WEIGHT);
        this.poolConfig = poolConfig;
        this.master = master;
    }

    @Override
    protected MultiJedisPool createResource() {
        Set<String> sentinels = ServerConfigure.isDeployEnv() ? poolConfig.getDeploySentinels() : poolConfig.getSentinels();
        return new MultiJedisPool(
            sentinels, master, poolConfig.isReadMaster(), poolConfig.getConnectTimeout(), poolConfig
        );
    }

    @Override
    public String getName() {
        return null;
    }

}
