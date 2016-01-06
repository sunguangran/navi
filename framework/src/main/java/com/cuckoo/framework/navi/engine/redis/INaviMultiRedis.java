package com.cuckoo.framework.navi.engine.redis;

import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

/**
 * 批量操作辅助接口
 */
public interface INaviMultiRedis {

    Transaction getTransaction();

    Pipeline getPipeline();

    void returnObject();

}
