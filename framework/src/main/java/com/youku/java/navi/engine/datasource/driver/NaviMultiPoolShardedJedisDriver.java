package com.youku.java.navi.engine.datasource.driver;

import com.youku.java.navi.common.ServerUrlUtil;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import com.youku.java.navi.engine.datasource.pool.ShardJedisPoolConfig;
import com.youku.java.navi.engine.redis.AbstractPoolBinaryShardedJedis;
import com.youku.java.navi.engine.redis.MultiJedisPoolInfo;
import com.youku.java.navi.engine.redis.MultiPoolBinaryShardedJedis;

import java.util.ArrayList;
import java.util.List;

public class NaviMultiPoolShardedJedisDriver extends
    AbstractNaviPoolJedisDriver {

    private MultiPoolBinaryShardedJedis jedis;
    private ShardJedisPoolConfig poolConfig;

    public NaviMultiPoolShardedJedisDriver(ServerUrlUtil.ServerUrl server, String auth,
                                           NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
        this.poolConfig = (ShardJedisPoolConfig) poolConfig;
        this.jedis = new MultiPoolBinaryShardedJedis(constructShardInfo(
            server.getUrl(), this.poolConfig));
    }

    private List<MultiJedisPoolInfo> constructShardInfo(String servers,
                                                        ShardJedisPoolConfig poolConfig) {
        String[] hosts = servers.split(",");
        List<MultiJedisPoolInfo> list = new ArrayList<MultiJedisPoolInfo>();
        for (int i = 0; i < hosts.length; i++) {
            list.add(new MultiJedisPoolInfo(hosts[i], poolConfig));
        }
        return list;
    }

    public void destroy() {
        jedis.disconnect();
    }

    public boolean isAlive() {
        return true;
    }

    public boolean open() {
        return true;
    }

    @Override
    public void close() throws NaviSystemException {
        //donothing
    }

    @Override
    public AbstractPoolBinaryShardedJedis<?, ?> getJedis() {
        return jedis;
    }

}
