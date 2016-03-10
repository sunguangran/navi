package com.cuckoo.framework.navi.engine.datasource.driver;

import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;
import com.cuckoo.framework.navi.common.ServerUrlUtil.ServerUrl;
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


    public AbstractNaviJedisDriver(ServerUrl server, String auth) {
        super(server, auth);
    }

    public AbstractNaviJedisDriver(ServerUrl server, String auth,
                                   NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
    }

    abstract public Boolean exists(byte[] key);

    abstract public Long del(byte[]... keys);

    abstract public DataType type(byte[] key);

    abstract public Set<byte[]> keys(byte[] pattern);

    abstract public byte[] randomKey();

    abstract public void rename(byte[] oldName, byte[] newName);


    abstract public Boolean renameNX(byte[] oldName, byte[] newName);

    abstract public Boolean expire(byte[] key, long seconds);

    abstract public Boolean expireAt(byte[] key, long unixTime);

    abstract public Boolean persist(byte[] key);


    abstract public Boolean move(byte[] key, int dbIndex);

    abstract public Long ttl(byte[] key);

    abstract public List<byte[]> sort(byte[] key, SortParameters params);

    abstract public byte[] get(byte[] key);

    abstract public byte[] getSet(byte[] key, byte[] value);

    abstract public List<byte[]> mGet(byte[]... keys);

    abstract public String set(byte[] key, byte[] value);

    abstract public Long setNX(byte[] key, byte[] value);

    abstract public String setEx(byte[] key, long seconds, byte[] value);

    abstract public void mSet(Map<byte[], byte[]> tuple);

    abstract public void mSetNX(Map<byte[], byte[]> tuple);

    abstract public Long incr(byte[] key);

    abstract public Long incrBy(byte[] key, long value);

    abstract public Long decr(byte[] key);

    abstract public Long decrBy(byte[] key, long value);

    abstract public Long append(byte[] key, byte[] value);

    abstract public byte[] getRange(byte[] key, long begin, long end);

    abstract public Long setRange(byte[] key, byte[] value, long offset);

    abstract public Boolean getBit(byte[] key, long offset);

    abstract public Boolean setBit(byte[] key, long offset, boolean value);

    abstract public Long strLen(byte[] key);

    abstract public Long rPush(byte[] key, byte[]... values);

    abstract public Long lPush(byte[] key, byte[]... values);

    abstract public Long rPushX(byte[] key, byte[] value);

    abstract public Long lPushX(byte[] key, byte[] value);

    abstract public Long lLen(byte[] key);

    abstract public List<byte[]> lRange(byte[] key, long begin, long end);

    abstract public String lTrim(byte[] key, long begin, long end);

    abstract public byte[] lIndex(byte[] key, long index);

    abstract public Long lInsert(byte[] key, Position where, byte[] pivot, byte[] value);

    abstract public String lSet(byte[] key, long index, byte[] value);

    abstract public Long lRem(byte[] key, long count, byte[] value);

    abstract public byte[] lPop(byte[] key);

    abstract public byte[] rPop(byte[] key);

    abstract public byte[] bLPop(int timeout, byte[] key);

    abstract public byte[] bRPop(int timeout, byte[] key);

    abstract public byte[] rPopLPush(byte[] srcKey, byte[] dstKey);

    abstract public byte[] bRPopLPush(int timeout, byte[] srcKey, byte[] dstKey);

    abstract public Long sAdd(byte[] key, byte[]... value);

    abstract public Long sRem(byte[] key, byte[] value);

    abstract public byte[] sPop(byte[] key);

    abstract public Boolean sMove(byte[] srcKey, byte[] destKey, byte[] value);

    abstract public Long sCard(byte[] key);

    abstract public Boolean sIsMember(byte[] key, byte[] value);

    abstract public Set<byte[]> sInter(byte[]... keys);

    abstract public void sInterStore(byte[] destKey, byte[]... keys);

    abstract public Set<byte[]> sUnion(byte[]... keys);

    abstract public void sUnionStore(byte[] destKey, byte[]... keys);

    abstract public Set<byte[]> sDiff(byte[]... keys);

    abstract public void sDiffStore(byte[] destKey, byte[]... keys);

    abstract public Set<byte[]> sMembers(byte[] key);

    abstract public byte[] sRandMember(byte[] key);

    abstract public Long zAdd(byte[] key, double score, byte[] value);

    abstract public Long zBatchAdd(byte[] key, Map<byte[], Double> scoreMembers);

    abstract public Boolean zRem(byte[] key, byte[] value);

    abstract public Double zIncrBy(byte[] key, double increment, byte[] value);

    abstract public Long zRank(byte[] key, byte[] value);

    abstract public Long zRevRank(byte[] key, byte[] value);

    abstract public Set<byte[]> zRange(byte[] key, long begin, long end);

    abstract public Set<Tuple> zRangeWithScores(byte[] key, long begin, long end);

    abstract public Set<byte[]> zRangeByScore(byte[] key, double min, double max);

    abstract public Set<Tuple> zRangeByScoreWithScores(byte[] key, double min, double max);

    abstract public Set<byte[]> zRangeByScore(byte[] key, double min, double max,
                                              long offset, long count);

    abstract public Set<Tuple> zRangeByScoreWithScores(byte[] key, double min,
                                                       double max, long offset, long count);

    abstract public Set<byte[]> zRevRange(byte[] key, long begin, long end);

    abstract public Set<Tuple> zRevRangeWithScores(byte[] key, long begin, long end);

    abstract public Set<byte[]> zRevRangeByScore(byte[] key, double min, double max);

    abstract public Set<Tuple> zRevRangeByScoreWithScores(byte[] key, double min,
                                                          double max);

    abstract public Set<byte[]> zRevRangeByScore(byte[] key, double min, double max,
                                                 long offset, long count);

    abstract public Set<Tuple> zRevRangeByScoreWithScores(byte[] key, double min,
                                                          double max, long offset, long count);

    abstract public Long zCount(byte[] key, double min, double max);

    abstract public Long zCard(byte[] key);

    abstract public Double zScore(byte[] key, byte[] value);

    abstract public Long zRemRange(byte[] key, long begin, long end);

    abstract public Long zRemRangeByScore(byte[] key, double min, double max);

    abstract public Long zUnionStore(byte[] destKey, byte[]... sets);

    abstract public Long zUnionStore(byte[] destKey, Aggregate aggregate, int[] weights,
                                     byte[]... sets);

    abstract public Long zInterStore(byte[] destKey, byte[]... sets);

    abstract public Long zInterStore(byte[] destKey, Aggregate aggregate, int[] weights,
                                     byte[]... sets);

    abstract public Long hSet(byte[] key, byte[] field, byte[] value);

    abstract public Long hSetNX(byte[] key, byte[] field, byte[] value);

    abstract public byte[] hGet(byte[] key, byte[] field);

    abstract public List<byte[]> hMGet(byte[] key, byte[]... fields);

    abstract public String hMSet(byte[] key, Map<byte[], byte[]> hashes);

    abstract public Long hIncrBy(byte[] key, byte[] field, long delta);

    abstract public Boolean hExists(byte[] key, byte[] field);

    abstract public Long hDel(byte[] key, byte[]... fields);

    abstract public Long hLen(byte[] key);

    abstract public Set<byte[]> hKeys(byte[] key);

    abstract public List<byte[]> hVals(byte[] key);

    abstract public Map<byte[], byte[]> hGetAll(byte[] key);

    abstract public void multi();

    abstract public List<Object> exec();

    abstract public void discard();

    abstract public void watch(byte[]... keys);

    abstract public void unwatch();

    abstract public boolean isSubscribed();

    abstract public Subscription getSubscription();

    abstract public Long publish(byte[] channel, byte[] message);

    abstract public void subscribe(MessageListener listener, byte[]... channels);

    abstract public void pSubscribe(MessageListener listener, byte[]... patterns);

    abstract public void select(int dbIndex);

    abstract public byte[] echo(byte[] message);

    abstract public String ping();

    abstract public void bgWriteAof();

    abstract public void bgSave();

    abstract public Long lastSave();

    abstract public void save();

    abstract public Long dbSize();

    abstract public void flushDb();

    abstract public void flushAll();

    abstract public Properties info();

    abstract public void shutdown();

    abstract public List<String> getConfig(String pattern);

    abstract public void setConfig(String param, String value);

    abstract public void resetConfigStats();

    abstract public Object getNativeConnection();

    abstract public boolean isQueueing();

    abstract public boolean isPipelined();

    abstract public void openPipeline();

    abstract public List<Object> closePipeline();

}
