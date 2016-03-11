package com.youku.java.navi.engine.redis;

import redis.clients.jedis.Jedis;

import java.util.Collection;

public interface IRedisCollectionCommand<T> {

    Collection<T> doCommand(Jedis jedis, byte[] key, Object... args);

}
