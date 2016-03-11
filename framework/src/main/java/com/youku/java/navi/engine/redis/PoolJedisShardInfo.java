package com.youku.java.navi.engine.redis;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.util.ShardInfo;

@Setter
@Getter
public class PoolJedisShardInfo extends ShardInfo<JedisPool> {

    private JedisShardInfo info;
    private GenericObjectPoolConfig poolConfig;

    public PoolJedisShardInfo(JedisShardInfo info, GenericObjectPoolConfig poolConfig) {
        this.info = info;
        this.poolConfig = poolConfig;
    }

    @Override
    public int getWeight() {
        return info.getWeight();
    }

    @Override
    protected JedisPool createResource() {
        return new JedisPool(poolConfig, info.getHost(), info.getPort(), info.getTimeout(), info.getPassword());
    }

    @Override
    public String getName() {
        return info.getName();
    }

}
