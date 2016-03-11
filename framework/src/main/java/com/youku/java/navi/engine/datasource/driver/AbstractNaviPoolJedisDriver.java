package com.youku.java.navi.engine.datasource.driver;

import com.youku.java.navi.common.ServerUrlUtil;
import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import com.youku.java.navi.engine.redis.AbstractPoolBinaryShardedJedis;
import com.youku.java.navi.engine.redis.INaviMultiRedis;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisListCommands.Position;
import org.springframework.data.redis.connection.RedisZSetCommands.Aggregate;
import org.springframework.data.redis.connection.RedisZSetCommands.Tuple;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.connection.Subscription;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.SortingParams;

import java.util.*;

import static redis.clients.jedis.Protocol.toByteArray;

public abstract class AbstractNaviPoolJedisDriver extends AbstractNaviJedisDriver {


    public AbstractNaviPoolJedisDriver(ServerUrlUtil.ServerUrl server, String auth) {
        super(server, auth);
    }

    public AbstractNaviPoolJedisDriver(ServerUrlUtil.ServerUrl server, String auth,
                                       NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
    }

    abstract public AbstractPoolBinaryShardedJedis<?, ?> getJedis();

    public Boolean exists(byte[] key) {
        return getJedis().exists(key);
    }

    // TODO
    public Long del(byte[]... keys) {
        for (byte[] key : keys) {
            getJedis().del(key);
        }
        return 0L;
    }

    public DataType type(byte[] key) {
        return DataType.fromCode(getJedis().type(key));
    }

    public Set<byte[]> keys(byte[] pattern) {
        throw new UnsupportedOperationException();
    }

    public byte[] randomKey() {
        throw new UnsupportedOperationException();
    }

    public void rename(byte[] oldName, byte[] newName) {
        throw new UnsupportedOperationException();

    }

    public Boolean renameNX(byte[] oldName, byte[] newName) {
        throw new UnsupportedOperationException();
    }

    public Boolean expire(byte[] key, long seconds) {
        return getJedis().expire(key, (int) seconds) == 1;
    }

    public Boolean expireAt(byte[] key, long unixTime) {
        return getJedis().expireAt(key, unixTime) == 1;
    }

    public Boolean persist(byte[] key) {
        return getJedis().persist(key) == 1;
    }

    public Boolean move(byte[] key, int dbIndex) {
        return getJedis().move(key, dbIndex) == 1;
    }

    public Long ttl(byte[] key) {
        return getJedis().ttl(key);
    }

    public List<byte[]> sort(byte[] key, SortParameters params) {
        SortingParams sortParams = NaviShardedJedisUtils
            .convertSortParams(params);
        return getJedis() != null ? getJedis().sort(key, sortParams) : getJedis().sort(key);
    }

    public byte[] get(byte[] key) {
        return getJedis().get(key);
    }

    public byte[] getSet(byte[] key, byte[] value) {
        return getJedis().getSet(key, value);
    }

    public List<byte[]> mGet(byte[]... keys) {
        List<List<byte[]>> groups = groupKeys(keys);
        List<byte[]> result = new LinkedList<byte[]>();
        List<Response<byte[]>> responses = new LinkedList<Response<byte[]>>();
        for (List<byte[]> group : groups) {
            INaviMultiRedis multiRedis = getJedis().openPipeline(group.get(0));
            try {
                Pipeline pipe = multiRedis.getPipeline();
                for (byte[] key : group) {
                    Response<byte[]> res = pipe.get(key);
                    responses.add(res);
                }
                pipe.sync();
            } finally {
                multiRedis.returnObject();
            }
        }
        for (Response<byte[]> response : responses) {
            result.add(response.get());
        }
        return result;
    }

    public String set(byte[] key, byte[] value) {
        return getJedis().set(key, value);
    }

    public Long setNX(byte[] key, byte[] value) {
        return getJedis().setnx(key, value);
    }

    public String setEx(byte[] key, long seconds, byte[] value) {
        return getJedis().setex(key, (int) seconds, value);
    }

    public void mSet(Map<byte[], byte[]> tuple) {
        List<List<byte[]>> groups = groupKeys(tuple.keySet().toArray(new byte[0][]));
        for (List<byte[]> group : groups) {
            INaviMultiRedis multiRedis = getJedis().openPipeline(group.get(0));
            try {
                Pipeline pipe = multiRedis.getPipeline();
                for (byte[] key : group) {
                    pipe.set(key, tuple.get(key));
                }
                pipe.sync();
            } finally {
                multiRedis.returnObject();
            }
        }
    }

    public void mSetNX(Map<byte[], byte[]> tuple) {
        List<List<byte[]>> groups = groupKeys(tuple.keySet().toArray(new byte[0][]));
        for (List<byte[]> group : groups) {
            INaviMultiRedis multiRedis = getJedis().openPipeline(group.get(0));
            try {
                Pipeline pipe = multiRedis.getPipeline();
                for (byte[] key : group) {
                    pipe.setnx(key, tuple.get(key));
                }
                pipe.sync();
            } finally {
                multiRedis.returnObject();
            }
        }
    }

    public Long incr(byte[] key) {
        return getJedis().incr(key);
    }

    public Long incrBy(byte[] key, long value) {
        return getJedis().incrBy(key, value);
    }

    public Long decr(byte[] key) {
        return getJedis().decr(key);
    }

    public Long decrBy(byte[] key, long value) {
        return getJedis().decrBy(key, value);
    }

    public Long append(byte[] key, byte[] value) {
        return getJedis().append(key, value);
    }

    public byte[] getRange(byte[] key, long begin, long end) {
        return getJedis().getrange(key, begin, end);
    }

    public Long setRange(byte[] key, byte[] value, long offset) {
        return getJedis().setrange(key, offset, value);
    }

    public Boolean getBit(byte[] key, long offset) {
        return getJedis().getbit(key, offset);
    }

    public Boolean setBit(byte[] key, long offset, boolean value) {
        return getJedis().setbit(key, offset, toByteArray(value ? 1 : 0));
    }

    public Long strLen(byte[] key) {
        return getJedis().strlen(key);
    }

    public Long rPush(byte[] key, byte[]... values) {
        return getJedis().rpush(key, values);
    }

    public Long lPush(byte[] key, byte[]... values) {
        return getJedis().lpush(key, values);
    }

    public Long rPushX(byte[] key, byte[] value) {
        return getJedis().rpushx(key, value);
    }

    public Long lPushX(byte[] key, byte[] value) {
        return getJedis().lpushx(key, value);
    }

    public Long lLen(byte[] key) {
        return getJedis().llen(key);
    }

    public List<byte[]> lRange(byte[] key, long begin, long end) {
        return getJedis().lrange(key, (int) begin, (int) end);
    }

    public String lTrim(byte[] key, long begin, long end) {
        return getJedis().ltrim(key, (int) begin, (int) end);
    }

    public byte[] lIndex(byte[] key, long index) {
        return getJedis().lindex(key, (int) index);
    }

    public Long lInsert(byte[] key, Position where, byte[] pivot, byte[] value) {
        return getJedis().linsert(key, NaviShardedJedisUtils.convertPosition(where),
            pivot, value);
    }

    public String lSet(byte[] key, long index, byte[] value) {
        return getJedis().lset(key, (int) index, value);
    }

    public Long lRem(byte[] key, long count, byte[] value) {
        return getJedis().lrem(key, (int) count, value);
    }

    public byte[] lPop(byte[] key) {
        return getJedis().lpop(key);
    }

    public byte[] rPop(byte[] key) {
        return getJedis().rpop(key);
    }

    public byte[] bLPop(int timeout, byte[] key) {
        List<byte[]> re = getJedis().blpop(key, timeout);
        if (re != null) {
            return re.get(1);
        } else {
            return null;
        }
    }

    public byte[] bRPop(int timeout, byte[] key) {
        List<byte[]> re = getJedis().brpop(key, timeout);
        if (re != null) {
            return re.get(1);
        } else {
            return null;
        }
    }

    public byte[] rPopLPush(byte[] srcKey, byte[] dstKey) {
        throw new UnsupportedOperationException();
    }

    public byte[] bRPopLPush(int timeout, byte[] srcKey, byte[] dstKey) {
        throw new UnsupportedOperationException();
    }

    public Long sAdd(byte[] key, byte[]... value) {
        return getJedis().sadd(key, value);
    }

    public Long sRem(byte[] key, byte[] value) {
        return getJedis().srem(key, value);
    }

    public byte[] sPop(byte[] key) {
        return getJedis().spop(key);
    }

    public Boolean sMove(byte[] srcKey, byte[] destKey, byte[] value) {
        throw new UnsupportedOperationException();
    }

    public Long sCard(byte[] key) {
        return getJedis().scard(key);
    }

    public Boolean sIsMember(byte[] key, byte[] value) {
        return getJedis().sismember(key, value);
    }

    public Set<byte[]> sInter(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    public void sInterStore(byte[] destKey, byte[]... keys) {
        throw new UnsupportedOperationException();

    }

    public Set<byte[]> sUnion(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    public void sUnionStore(byte[] destKey, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    public Set<byte[]> sDiff(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    public void sDiffStore(byte[] destKey, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    public Set<byte[]> sMembers(byte[] key) {
        return getJedis().smembers(key);
    }

    public byte[] sRandMember(byte[] key) {
        return getJedis().srandmember(key);
    }

    public Long zAdd(byte[] key, double score, byte[] value) {
        return getJedis().zadd(key, score, value);
    }

    public Long zBatchAdd(byte[] key, Map<byte[], Double> scoreMembers) {
        return getJedis().zadd(key, scoreMembers);
    }

    public Boolean zRem(byte[] key, byte[] value) {
        return NaviShardedJedisUtils.convertCodeReply(getJedis().zrem(key, value));
    }

    public Double zIncrBy(byte[] key, double increment, byte[] value) {
        return getJedis().zincrby(key, increment, value);
    }

    public Long zRank(byte[] key, byte[] value) {
        return getJedis().zrank(key, value);
    }

    public Long zRevRank(byte[] key, byte[] value) {
        return getJedis().zrevrank(key, value);
    }

    public Set<byte[]> zRange(byte[] key, long begin, long end) {
        return getJedis().zrange(key, (int) begin, (int) end);
    }

    public Set<Tuple> zRangeWithScores(byte[] key, long begin, long end) {
        return NaviShardedJedisUtils.convertJedisTuple(getJedis()
            .zrevrangeWithScores(key, (int) begin, (int) end));
    }

    public Set<byte[]> zRangeByScore(byte[] key, double min, double max) {
        return getJedis().zrangeByScore(key, min, max);
    }

    public Set<Tuple> zRangeByScoreWithScores(byte[] key, double min, double max) {
        return NaviShardedJedisUtils.convertJedisTuple(getJedis()
            .zrangeByScoreWithScores(key, min, max));
    }

    public Set<byte[]> zRangeByScore(byte[] key, double min, double max,
                                     long offset, long count) {
        return getJedis().zrangeByScore(key, min, max, (int) offset, (int) count);
    }

    public Set<Tuple> zRangeByScoreWithScores(byte[] key, double min,
                                              double max, long offset, long count) {
        return NaviShardedJedisUtils.convertJedisTuple(getJedis()
            .zrangeByScoreWithScores(key, min, max, (int) offset,
                (int) count));
    }

    public Set<byte[]> zRevRange(byte[] key, long begin, long end) {
        return getJedis().zrevrange(key, (int) begin, (int) end);
    }

    public Set<Tuple> zRevRangeWithScores(byte[] key, long begin, long end) {
        return NaviShardedJedisUtils.convertJedisTuple(getJedis()
            .zrevrangeWithScores(key, (int) begin, (int) end));
    }

    public Set<byte[]> zRevRangeByScore(byte[] key, double min, double max) {
        return getJedis().zrevrangeByScore(key, max, min);
    }

    public Set<Tuple> zRevRangeByScoreWithScores(byte[] key, double min,
                                                 double max) {
        return NaviShardedJedisUtils.convertJedisTuple(getJedis()
            .zrevrangeByScoreWithScores(key, max, min));
    }

    public Set<byte[]> zRevRangeByScore(byte[] key, double min, double max,
                                        long offset, long count) {
        return getJedis().zrevrangeByScore(key, max, min, (int) offset, (int) count);
    }

    public Set<Tuple> zRevRangeByScoreWithScores(byte[] key, double min,
                                                 double max, long offset, long count) {
        return NaviShardedJedisUtils.convertJedisTuple(getJedis()
            .zrevrangeByScoreWithScores(key, max, min, (int) offset,
                (int) count));
    }

    public Long zCount(byte[] key, double min, double max) {
        return getJedis().zcount(key, min, max);
    }

    public Long zCard(byte[] key) {
        return getJedis().zcard(key);
    }

    public Double zScore(byte[] key, byte[] value) {
        return getJedis().zscore(key, value);
    }

    public Long zRemRange(byte[] key, long begin, long end) {
        return getJedis().zremrangeByRank(key, (int) begin, (int) end);
    }

    public Long zRemRangeByScore(byte[] key, double min, double max) {
        return getJedis().zremrangeByScore(key, min, max);
    }

    public Long zUnionStore(byte[] destKey, byte[]... sets) {
        throw new UnsupportedOperationException();
    }

    public Long zUnionStore(byte[] destKey, Aggregate aggregate, int[] weights,
                            byte[]... sets) {
        throw new UnsupportedOperationException();
    }

    public Long zInterStore(byte[] destKey, byte[]... sets) {
        throw new UnsupportedOperationException();
    }

    public Long zInterStore(byte[] destKey, Aggregate aggregate, int[] weights,
                            byte[]... sets) {
        throw new UnsupportedOperationException();
    }

    public Long hSet(byte[] key, byte[] field, byte[] value) {
        return getJedis().hset(key, field, value);
    }

    public Long hSetNX(byte[] key, byte[] field, byte[] value) {
        return getJedis().hsetnx(key, field, value);
    }

    public byte[] hGet(byte[] key, byte[] field) {
        return getJedis().hget(key, field);
    }

    public List<byte[]> hMGet(byte[] key, byte[]... fields) {
        return getJedis().hmget(key, fields);
    }

    public String hMSet(byte[] key, Map<byte[], byte[]> hashes) {
        return getJedis().hmset(key, hashes);
    }

    public Long hIncrBy(byte[] key, byte[] field, long delta) {
        return getJedis().hincrBy(key, field, delta);
    }

    public Boolean hExists(byte[] key, byte[] field) {
        return getJedis().hexists(key, field);
    }

    public Long hDel(byte[] key, byte[]... fields) {
        return getJedis().hdel(key, fields);
    }

    public Long hLen(byte[] key) {
        return getJedis().hlen(key);
    }

    public Set<byte[]> hKeys(byte[] key) {
        return getJedis().hkeys(key);
    }

    public List<byte[]> hVals(byte[] key) {
        return new LinkedList<byte[]>(getJedis().hvals(key));
    }

    public Map<byte[], byte[]> hGetAll(byte[] key) {
        return getJedis().hgetAll(key);
    }

    public void multi() {
        throw new UnsupportedOperationException();
    }

    public List<Object> exec() {
        throw new UnsupportedOperationException();
    }

    public void discard() {
        throw new UnsupportedOperationException();
    }

    public void watch(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    public void unwatch() {
        throw new UnsupportedOperationException();
    }

    public boolean isSubscribed() {
        return false;
    }

    public Subscription getSubscription() {
        throw new UnsupportedOperationException();
    }

    public Long publish(byte[] channel, byte[] message) {
        throw new UnsupportedOperationException();
    }

    public void subscribe(MessageListener listener, byte[]... channels) {
        throw new UnsupportedOperationException();

    }

    public void pSubscribe(MessageListener listener, byte[]... patterns) {
        throw new UnsupportedOperationException();
    }

    public void select(int dbIndex) {
        throw new UnsupportedOperationException();

    }

    public byte[] echo(byte[] message) {
        throw new UnsupportedOperationException();
    }

    public String ping() {
        throw new UnsupportedOperationException();
    }

    public void bgWriteAof() {
        throw new UnsupportedOperationException();
    }

    public void bgSave() {
        throw new UnsupportedOperationException();
    }

    public Long lastSave() {
        throw new UnsupportedOperationException();
    }

    public void save() {
        throw new UnsupportedOperationException();
    }

    public Long dbSize() {
        throw new UnsupportedOperationException();
    }

    public void flushDb() {
        throw new UnsupportedOperationException();
    }

    public void flushAll() {
        throw new UnsupportedOperationException();
    }

    public Properties info() {
        throw new UnsupportedOperationException();
    }

    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    public List<String> getConfig(String pattern) {
        throw new UnsupportedOperationException();
    }

    public void setConfig(String param, String value) {
        throw new UnsupportedOperationException();

    }

    public void resetConfigStats() {
        throw new UnsupportedOperationException();
    }

    public Object getNativeConnection() {
        throw new UnsupportedOperationException();
    }

    public boolean isQueueing() {
        throw new UnsupportedOperationException();
    }

    public boolean isPipelined() {
        return false;
    }

    public void openPipeline() {
        throw new UnsupportedOperationException();
    }

    public List<Object> closePipeline() {
        throw new UnsupportedOperationException(
            "is not be surpported in the distributed envriment!");
    }

    public INaviMultiRedis multi(byte[] key) {
        return getJedis().multi(key);
    }

    public INaviMultiRedis openPipeline(byte[] key) {
        return getJedis().openPipeline(key);
    }

    public Object eval(byte[] key, byte[] script, int keyCount, byte[]... params) {
        return getJedis().eval(key, script, keyCount, params);
    }

    public Object evalsha(byte[] key, byte[] sha, int keyCount, byte[]... params) {
        return getJedis().evalsha(key, sha, keyCount, params);
    }

    public List<List<byte[]>> groupKeys(byte[][] keys) {
        return getJedis().groupKeys(keys);
    }
}
