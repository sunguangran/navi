package com.youku.java.navi.engine.redis;

import com.youku.java.navi.engine.datasource.pool.ShardJedisPoolConfig;
import com.youku.java.navi.server.ServerConfigure;
import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.ShardInfo;
import redis.clients.util.Sharded;

import java.util.Set;

@Setter
@Getter
public class SentinelPoolJedisShardInfo extends ShardInfo<JedisSentinelPool> {

    private ShardJedisPoolConfig poolConfig;
    private String master;

    public SentinelPoolJedisShardInfo(String master, ShardJedisPoolConfig poolConfig) {
        this(master, poolConfig, Sharded.DEFAULT_WEIGHT);
    }

    public SentinelPoolJedisShardInfo(String master, ShardJedisPoolConfig poolConfig, int weight) {
        super(weight);
        this.poolConfig = poolConfig;
        this.master = master;
    }

    @Override
    protected JedisSentinelPool createResource() {
        Set<String> sentinels = ServerConfigure.isDeployEnv() ? poolConfig.getDeploySentinels() : poolConfig.getSentinels();
        return new JedisSentinelPool(this.master, sentinels, poolConfig, poolConfig.getConnectTimeout(), null);
    }

    @Override
    public String getName() {
        return null;
    }

}
