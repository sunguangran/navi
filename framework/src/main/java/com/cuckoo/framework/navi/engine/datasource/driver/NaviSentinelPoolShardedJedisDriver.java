package com.cuckoo.framework.navi.engine.datasource.driver;

import com.cuckoo.framework.navi.common.exception.NaviSystemException;
import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;
import com.cuckoo.framework.navi.engine.datasource.pool.ShardJedisPoolConfig;
import com.cuckoo.framework.navi.engine.redis.AbstractPoolBinaryShardedJedis;
import com.cuckoo.framework.navi.engine.redis.SentinelPoolBinaryShardedJedis;
import com.cuckoo.framework.navi.engine.redis.SentinelPoolJedisShardInfo;
import com.cuckoo.framework.navi.common.ServerAddress;

import java.util.ArrayList;
import java.util.List;

public class NaviSentinelPoolShardedJedisDriver extends ANaviPoolJedisDriver {

    private SentinelPoolBinaryShardedJedis jedis;
    private ShardJedisPoolConfig poolConfig;


    public NaviSentinelPoolShardedJedisDriver(ServerAddress server, String auth, NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
        this.poolConfig = (ShardJedisPoolConfig) poolConfig;
        this.jedis = new SentinelPoolBinaryShardedJedis(constructShardInfo(
            server.getUrl(), this.poolConfig));
    }

    private List<SentinelPoolJedisShardInfo> constructShardInfo(String servers,
                                                                ShardJedisPoolConfig poolConfig) {
        String[] hosts = servers.split(",");
        List<SentinelPoolJedisShardInfo> list = new ArrayList<SentinelPoolJedisShardInfo>();
        for (int i = 0; i < hosts.length; i++) {
            list.add(new SentinelPoolJedisShardInfo(hosts[i], poolConfig));
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
