package com.cuckoo.framework.navi.engine.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.util.Pool;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PoolBinaryShardedJedis extends AbstractPoolBinaryShardedJedis<JedisPool, PoolJedisShardInfo> {

    public PoolBinaryShardedJedis(List<PoolJedisShardInfo> shards) {
        super(shards);
    }

    public void disconnect() {
        for (JedisPool jedis : getAllShards()) {
            jedis.destroy();
        }
    }

    @Override
    public Pool<Jedis> getPool(byte[] key) {
        return getShard(key);
    }

    @Override
    public List<List<byte[]>> groupKeys(byte[][] keys) {
        List<List<byte[]>> groups = new LinkedList<>();

        if (keys.length == 0) {
            return groups;
        }

        if (keys.length == 1) {
            List<byte[]> group = new LinkedList<>();
            group.add(keys[0]);
            groups.add(group);
            return groups;
        }

        Map<Pool<Jedis>, List<byte[]>> map = new HashMap<>();
        for (byte[] key : keys) {
            Pool<Jedis> multiJedis = getShard(key);
            List<byte[]> group = map.get(multiJedis);
            if (group == null) {
                group = new LinkedList<>();
            }
            group.add(key);
            map.put(multiJedis, group);
        }
        groups.addAll(map.values());
        return groups;
    }
}
