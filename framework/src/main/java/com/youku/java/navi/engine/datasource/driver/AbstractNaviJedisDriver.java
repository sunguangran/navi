package com.youku.java.navi.engine.datasource.driver;

import com.youku.java.navi.common.ServerUrlUtil;
import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisListCommands.Position;
import org.springframework.data.redis.connection.RedisZSetCommands.Aggregate;
import org.springframework.data.redis.connection.RedisZSetCommands.Tuple;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.connection.Subscription;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public abstract class AbstractNaviJedisDriver extends AbstractNaviDriver {

    public AbstractNaviJedisDriver(ServerUrlUtil.ServerUrl server, String auth) {
        super(server, auth);
    }

    public AbstractNaviJedisDriver(ServerUrlUtil.ServerUrl server, String auth, NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
    }

    public abstract Boolean exists(byte[] key);

    public abstract Long del(byte[]... keys);

    public abstract DataType type(byte[] key);

    public abstract Set<byte[]> keys(byte[] pattern);

    public abstract byte[] randomKey();

    public abstract void rename(byte[] oldName, byte[] newName);

    public abstract Boolean renameNX(byte[] oldName, byte[] newName);

    public abstract Boolean expire(byte[] key, long seconds);

    public abstract Boolean expireAt(byte[] key, long unixTime);

    public abstract Boolean persist(byte[] key);

    public abstract Boolean move(byte[] key, int dbIndex);

    public abstract Long ttl(byte[] key);

    public abstract List<byte[]> sort(byte[] key, SortParameters params);

    public abstract byte[] get(byte[] key);

    public abstract byte[] getSet(byte[] key, byte[] value);

    public abstract List<byte[]> mGet(byte[]... keys);

    public abstract String set(byte[] key, byte[] value);

    public abstract Long setNX(byte[] key, byte[] value);

    public abstract String setEx(byte[] key, long seconds, byte[] value);

    public abstract void mSet(Map<byte[], byte[]> tuple);

    public abstract void mSetNX(Map<byte[], byte[]> tuple);

    public abstract Long incr(byte[] key);

    public abstract Long incrBy(byte[] key, long value);

    public abstract Long decr(byte[] key);

    public abstract Long decrBy(byte[] key, long value);

    public abstract Long append(byte[] key, byte[] value);

    public abstract byte[] getRange(byte[] key, long begin, long end);

    public abstract Long setRange(byte[] key, byte[] value, long offset);

    public abstract Boolean getBit(byte[] key, long offset);

    public abstract Boolean setBit(byte[] key, long offset, boolean value);

    public abstract Long strLen(byte[] key);

    public abstract Long rPush(byte[] key, byte[]... values);

    public abstract Long lPush(byte[] key, byte[]... values);

    public abstract Long rPushX(byte[] key, byte[] value);

    public abstract Long lPushX(byte[] key, byte[] value);

    public abstract Long lLen(byte[] key);

    public abstract List<byte[]> lRange(byte[] key, long begin, long end);

    public abstract String lTrim(byte[] key, long begin, long end);

    public abstract byte[] lIndex(byte[] key, long index);

    public abstract Long lInsert(byte[] key, Position where, byte[] pivot, byte[] value);

    public abstract String lSet(byte[] key, long index, byte[] value);

    public abstract Long lRem(byte[] key, long count, byte[] value);

    public abstract byte[] lPop(byte[] key);

    public abstract byte[] rPop(byte[] key);

    public abstract byte[] bLPop(int timeout, byte[] key);

    public abstract byte[] bRPop(int timeout, byte[] key);

    public abstract byte[] rPopLPush(byte[] srcKey, byte[] dstKey);

    public abstract byte[] bRPopLPush(int timeout, byte[] srcKey, byte[] dstKey);

    public abstract Long sAdd(byte[] key, byte[]... value);

    public abstract Long sRem(byte[] key, byte[] value);

    public abstract byte[] sPop(byte[] key);

    public abstract Boolean sMove(byte[] srcKey, byte[] destKey, byte[] value);

    public abstract Long sCard(byte[] key);

    public abstract Boolean sIsMember(byte[] key, byte[] value);

    public abstract Set<byte[]> sInter(byte[]... keys);

    public abstract void sInterStore(byte[] destKey, byte[]... keys);

    public abstract Set<byte[]> sUnion(byte[]... keys);

    public abstract void sUnionStore(byte[] destKey, byte[]... keys);

    public abstract Set<byte[]> sDiff(byte[]... keys);

    public abstract void sDiffStore(byte[] destKey, byte[]... keys);

    public abstract Set<byte[]> sMembers(byte[] key);

    public abstract byte[] sRandMember(byte[] key);

    public abstract Long zAdd(byte[] key, double score, byte[] value);

    public abstract Long zBatchAdd(byte[] key, Map<byte[], Double> scoreMembers);

    public abstract Boolean zRem(byte[] key, byte[] value);

    public abstract Double zIncrBy(byte[] key, double increment, byte[] value);

    public abstract Long zRank(byte[] key, byte[] value);

    public abstract Long zRevRank(byte[] key, byte[] value);

    public abstract Set<byte[]> zRange(byte[] key, long begin, long end);

    public abstract Set<Tuple> zRangeWithScores(byte[] key, long begin, long end);

    public abstract Set<byte[]> zRangeByScore(byte[] key, double min, double max);

    public abstract Set<Tuple> zRangeByScoreWithScores(byte[] key, double min, double max);

    public abstract Set<byte[]> zRangeByScore(byte[] key, double min, double max,
                                              long offset, long count);

    public abstract Set<Tuple> zRangeByScoreWithScores(byte[] key, double min,
                                                       double max, long offset, long count);

    public abstract Set<byte[]> zRevRange(byte[] key, long begin, long end);

    public abstract Set<Tuple> zRevRangeWithScores(byte[] key, long begin, long end);

    public abstract Set<byte[]> zRevRangeByScore(byte[] key, double min, double max);

    public abstract Set<Tuple> zRevRangeByScoreWithScores(byte[] key, double min,
                                                          double max);

    public abstract Set<byte[]> zRevRangeByScore(byte[] key, double min, double max,
                                                 long offset, long count);

    public abstract Set<Tuple> zRevRangeByScoreWithScores(byte[] key, double min,
                                                          double max, long offset, long count);

    public abstract Long zCount(byte[] key, double min, double max);

    public abstract Long zCard(byte[] key);

    public abstract Double zScore(byte[] key, byte[] value);

    public abstract Long zRemRange(byte[] key, long begin, long end);

    public abstract Long zRemRangeByScore(byte[] key, double min, double max);

    public abstract Long zUnionStore(byte[] destKey, byte[]... sets);

    public abstract Long zUnionStore(byte[] destKey, Aggregate aggregate, int[] weights,
                                     byte[]... sets);

    public abstract Long zInterStore(byte[] destKey, byte[]... sets);

    public abstract Long zInterStore(byte[] destKey, Aggregate aggregate, int[] weights,
                                     byte[]... sets);

    public abstract Long hSet(byte[] key, byte[] field, byte[] value);

    public abstract Long hSetNX(byte[] key, byte[] field, byte[] value);

    public abstract byte[] hGet(byte[] key, byte[] field);

    public abstract List<byte[]> hMGet(byte[] key, byte[]... fields);

    public abstract String hMSet(byte[] key, Map<byte[], byte[]> hashes);

    public abstract Long hIncrBy(byte[] key, byte[] field, long delta);

    public abstract Boolean hExists(byte[] key, byte[] field);

    public abstract Long hDel(byte[] key, byte[]... fields);

    public abstract Long hLen(byte[] key);

    public abstract Set<byte[]> hKeys(byte[] key);

    public abstract List<byte[]> hVals(byte[] key);

    public abstract Map<byte[], byte[]> hGetAll(byte[] key);

    public abstract void multi();

    public abstract List<Object> exec();

    public abstract void discard();

    public abstract void watch(byte[]... keys);

    public abstract void unwatch();

    public abstract boolean isSubscribed();

    public abstract Subscription getSubscription();

    public abstract Long publish(byte[] channel, byte[] message);

    public abstract void subscribe(MessageListener listener, byte[]... channels);

    public abstract void pSubscribe(MessageListener listener, byte[]... patterns);

    public abstract void select(int dbIndex);

    public abstract byte[] echo(byte[] message);

    public abstract String ping();

    public abstract void bgWriteAof();

    public abstract void bgSave();

    public abstract Long lastSave();

    public abstract void save();

    public abstract Long dbSize();

    public abstract void flushDb();

    public abstract void flushAll();

    public abstract Properties info();

    public abstract void shutdown();

    public abstract List<String> getConfig(String pattern);

    public abstract void setConfig(String param, String value);

    public abstract void resetConfigStats();

    public abstract Object getNativeConnection();

    public abstract boolean isQueueing();

    public abstract boolean isPipelined();

    public abstract void openPipeline();

    public abstract List<Object> closePipeline();

}
