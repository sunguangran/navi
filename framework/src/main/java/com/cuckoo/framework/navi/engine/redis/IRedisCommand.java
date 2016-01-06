package com.cuckoo.framework.navi.engine.redis;

import redis.clients.jedis.Jedis;

public interface IRedisCommand {

    Object doCommand(Jedis jedis, byte[] key, Object... args);

}
