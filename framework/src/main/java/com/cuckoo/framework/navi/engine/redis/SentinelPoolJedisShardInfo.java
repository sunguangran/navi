package com.cuckoo.framework.navi.engine.redis;

import com.cuckoo.framework.navi.engine.datasource.pool.ShardJedisPoolConfig;
import com.cuckoo.framework.navi.server.ServerConfigure;
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
        super(Sharded.DEFAULT_WEIGHT);
        this.poolConfig = poolConfig;
        this.master = master;
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
