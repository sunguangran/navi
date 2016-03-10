package com.cuckoo.framework.navi.engine.datasource.driver;

import com.cuckoo.framework.navi.common.NaviSystemException;
import com.cuckoo.framework.navi.common.ServerUrlUtil;
import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;
import com.cuckoo.framework.navi.engine.datasource.pool.ShardJedisPoolConfig;
import com.cuckoo.framework.navi.engine.redis.AbstractPoolBinaryShardedJedis;
import com.cuckoo.framework.navi.engine.redis.PoolBinaryShardedJedis;
import com.cuckoo.framework.navi.engine.redis.PoolJedisShardInfo;
import redis.clients.jedis.JedisShardInfo;

import java.util.ArrayList;
import java.util.List;

public class NaviPoolShardedJedisDriver extends AbstractNaviPoolJedisDriver {

    private PoolBinaryShardedJedis jedis;
    private ShardJedisPoolConfig poolConfig;

    public NaviPoolShardedJedisDriver(ServerUrlUtil.ServerUrl server, String auth, NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
        this.poolConfig = (ShardJedisPoolConfig) poolConfig;
        this.jedis = new PoolBinaryShardedJedis(constructShardInfo(
            server.getUrl(), this.poolConfig));
    }

    private List<PoolJedisShardInfo> constructShardInfo(String servers,
                                                        ShardJedisPoolConfig poolConfig) {
        String[] hosts = servers.split(",");
        List<PoolJedisShardInfo> list = new ArrayList<PoolJedisShardInfo>();
        for (int i = 0; i < hosts.length; i++) {
            String[] hostPairs = hosts[i].split(":");
            JedisShardInfo shardInfo = new JedisShardInfo(hostPairs[0],
                Integer.valueOf(hostPairs[1]),
                poolConfig.getConnectTimeout());
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
