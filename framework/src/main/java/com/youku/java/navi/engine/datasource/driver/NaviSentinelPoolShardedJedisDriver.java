package com.youku.java.navi.engine.datasource.driver;

import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import com.youku.java.navi.engine.datasource.pool.ShardJedisPoolConfig;
import com.youku.java.navi.engine.redis.AbstractPoolBinaryShardedJedis;
import com.youku.java.navi.engine.redis.SentinelPoolJedisShardInfo;
import com.youku.java.navi.engine.redis.SentinelPoolBinaryShardedJedis;
import com.youku.java.navi.common.ServerUrlUtil;

import java.util.ArrayList;
import java.util.List;

public class NaviSentinelPoolShardedJedisDriver extends AbstractNaviPoolJedisDriver {

    private SentinelPoolBinaryShardedJedis jedis;
    private ShardJedisPoolConfig poolConfig;


    public NaviSentinelPoolShardedJedisDriver(ServerUrlUtil.ServerUrl server, String auth, NaviPoolConfig poolConfig) {
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
