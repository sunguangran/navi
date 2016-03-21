package com.youku.java.navi.engine.datasource.driver;

import com.youku.java.navi.common.ServerUrlUtil;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import com.youku.java.navi.engine.datasource.pool.ShardJedisPoolConfig;
import com.youku.java.navi.engine.redis.AbstractPoolBinaryShardedJedis;
import com.youku.java.navi.engine.redis.PoolBinaryShardedJedis;
import com.youku.java.navi.engine.redis.PoolJedisShardInfo;
import redis.clients.jedis.JedisShardInfo;

import java.util.ArrayList;
import java.util.List;

public class NaviPoolShardedJedisDriver extends AbstractNaviPoolJedisDriver {

    private PoolBinaryShardedJedis jedis;
    private ShardJedisPoolConfig poolConfig;

    public NaviPoolShardedJedisDriver(ServerUrlUtil.ServerUrl server, String auth, NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
        this.poolConfig = (ShardJedisPoolConfig) poolConfig;
        this.jedis = new PoolBinaryShardedJedis(constructShardInfo(server.getUrl(), this.poolConfig));
    }

    private List<PoolJedisShardInfo> constructShardInfo(String servers, ShardJedisPoolConfig poolConfig) {
        String[] hosts = servers.split(",");
        List<PoolJedisShardInfo> list = new ArrayList<>();
        for (String host : hosts) {
            String[] hostPairs = host.split(":");
            JedisShardInfo shardInfo = new JedisShardInfo(
                hostPairs[0], Integer.valueOf(hostPairs[1]), poolConfig.getConnectTimeout()
            );
            list.add(new PoolJedisShardInfo(shardInfo, poolConfig));
        }

        return list;
    }

    public void destroy() throws NaviSystemException {
        jedis.disconnect();
    }

    public boolean isAlive() throws NaviSystemException {
        return true;
    }

    public boolean open() {
        return true;
    }

    @Override
    public void close() throws NaviSystemException {
        //do nothing
    }

    @Override
    public AbstractPoolBinaryShardedJedis<?, ?> getJedis() {
        return jedis;
    }

}
