package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.common.NAVIERROR;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.core.INaviCache;
import com.youku.java.navi.engine.core.INaviDriver;
import com.youku.java.navi.engine.datasource.driver.NaviShardJedisDriver;
import com.youku.java.navi.engine.redis.INaviMultiRedis;
import com.youku.java.navi.utils.AlibabaJsonSerializer;
import org.springframework.data.redis.connection.RedisZSetCommands.Tuple;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

import java.util.*;

/**
 */
public class NaviShardedJedisService extends AbstractNaviDataService implements
    INaviCache {

    private AlibabaJsonSerializer jsonSerializer = new AlibabaJsonSerializer();

    public NaviShardJedisDriver getDriver() {
        INaviDriver driver = dataSource.getHandle();
        if (driver instanceof NaviShardJedisDriver) {
            return (NaviShardJedisDriver) driver;
        }
        driver.close();
        throw new NaviSystemException("the driver is invalid!",
            NAVIERROR.SYSERROR.code());
    }

    private <K> byte[] object2Bytes(K k) {
        return jsonSerializer.getJSONBytes(k);
    }

    private <K> byte[][] objArray2BytesArray(K[] keys) {
        byte[][] bytes = new byte[keys.length][];
        for (int i = 0; i < keys.length; i++) {
            bytes[i] = jsonSerializer.getJSONBytes(keys[i]);
        }
        return bytes;
    }

    private <K> K bytes2Object(byte[] bytes, Class<K> classNm) {
        return jsonSerializer.getObjectFromBytes(bytes, classNm);
    }

    private <V> List<V> bytesList2ObjList(List<byte[]> list, Class<V> classNm) {
        List<V> objList = new ArrayList<V>();
        for (byte[] bytes : list) {
            objList.add(jsonSerializer.getObjectFromBytes(bytes, classNm));
        }
        return objList;
    }

    public <K> boolean expire(K key, long timeout) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.expire(object2Bytes(key), timeout).booleanValue();
        } finally {
            driver.close();
        }
    }

    public <K, V> boolean set(K key, V val) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.set(object2Bytes(key), object2Bytes(val)).equals("OK");
        } finally {
            driver.close();
        }
    }

    public <K, V> Long setnx(K key, V val) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.setNX(object2Bytes(key), object2Bytes(val));
        } finally {
            driver.close();
        }
    }

    public <K, V> boolean setex(K key, V val, long timeout) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.setEx(object2Bytes(key), timeout, object2Bytes(val)).equals("OK");
        } finally {
            driver.close();
        }
    }

    public <K, V> V getSet(K key, V val, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            byte[] bytes = driver.getSet(object2Bytes(key), object2Bytes(val));
            return bytes2Object(bytes, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V> V get(K key, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            byte[] bytes = driver.get(object2Bytes(key));
            return bytes2Object(bytes, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V> List<V> MGet(Class<V> classNm, K... keys) {
        NaviShardJedisDriver driver = getDriver();
        try {
            List<byte[]> list = driver.mGet(objArray2BytesArray(keys));
            return bytesList2ObjList(list, classNm);
        } finally {
            driver.close();
        }
    }

    public <K> Long incr(K key, long delta) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.incrBy(object2Bytes(key), delta);
        } finally {
            driver.close();
        }
    }

    public <K> Long decr(K key, long delta) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.decrBy(object2Bytes(key), delta);
        } finally {
            driver.close();
        }
    }

    public <K> boolean exists(K key) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.exists(object2Bytes(key));
        } finally {
            driver.close();
        }
    }

    public <K> Long delete(K... keys) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.del(objArray2BytesArray(keys));
        } finally {
            driver.close();
        }
    }

    public <K, V> Long zBatchAdd(K key, Map<V, Double> map) {
        NaviShardJedisDriver driver = getDriver();
        try {
            Map<byte[], Double> scoreMembers = new HashMap<byte[], Double>();
            for (V v : map.keySet()) {
                scoreMembers.put(object2Bytes(v), map.get(v));
            }
            return driver.zBatchAdd(object2Bytes(key), scoreMembers);
        } finally {
            driver.close();
        }
    }

    public <K, V> Long zadd(K key, V val, double score) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.zAdd(object2Bytes(key), score, object2Bytes(val));
        } finally {
            driver.close();
        }
    }

    public <K, V> Double zscore(K key, V val) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.zScore(object2Bytes(key), object2Bytes(val));
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<V> zRevRangeByScore(K key, double min, double max,
                                          long limit, long skip, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            Set<byte[]> byteset = limit > 0 ? driver.zRevRangeByScore(
                object2Bytes(key), min, max, skip, limit) : driver
                .zRevRangeByScore(object2Bytes(key), min, max);
            Set<V> set = new LinkedHashSet<V>();
            for (byte[] bytes : byteset) {
                set.add(bytes2Object(bytes, classNm));
            }
            return set;
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<TypedTuple<V>> zRevRangeByScoreWithScore(K key, double min,
                                                               double max, long limit, long skip, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            Set<Tuple> set = limit > 0 ? driver.zRevRangeByScoreWithScores(
                object2Bytes(key), min, max, skip, limit) : driver
                .zRevRangeByScoreWithScores(object2Bytes(key), min, max);
            Set<TypedTuple<V>> result = new LinkedHashSet<TypedTuple<V>>();
            for (Tuple t : set) {
                result.add(new DefaultTypedTuple<V>(bytes2Object(t.getValue(),
                    classNm), t.getScore()));
            }
            return result;
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<V> zRangeByScore(K key, double min, double max,
                                       long limit, long skip, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            Set<byte[]> byteset = limit > 0 ? driver.zRangeByScore(
                object2Bytes(key), min, max, skip, limit) : driver
                .zRangeByScore(object2Bytes(key), min, max);
            Set<V> set = new LinkedHashSet<V>();
            for (byte[] bytes : byteset) {
                set.add(bytes2Object(bytes, classNm));
            }
            return set;
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<TypedTuple<V>> zRangeByScoreWithScore(K key, double min,
                                                            double max, long limit, long skip, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            Set<Tuple> set = limit > 0 ? driver.zRangeByScoreWithScores(
                object2Bytes(key), min, max, skip, limit) : driver
                .zRangeByScoreWithScores(object2Bytes(key), min, max);
            Set<TypedTuple<V>> result = new LinkedHashSet<TypedTuple<V>>();
            for (Tuple t : set) {
                result.add(new DefaultTypedTuple<V>(bytes2Object(t.getValue(),
                    classNm), t.getScore()));
            }
            return result;
        } finally {
            driver.close();
        }
    }

    public <K> Long zCount(K key, double min, double max) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.zCount(object2Bytes(key), min, max);
        } finally {
            driver.close();
        }
    }

    public <K> Long zSize(K key) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.zCard(object2Bytes(key));
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<V> zRange(K key, long start, long end, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            Set<byte[]> byteset = driver.zRange(object2Bytes(key), start, end);
            Set<V> set = new LinkedHashSet<V>();
            for (byte[] bytes : byteset) {
                set.add(bytes2Object(bytes, classNm));
            }
            return set;
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<TypedTuple<V>> zRangeWithScore(K key, long start,
                                                     long end, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            Set<Tuple> set = driver.zRangeWithScores(object2Bytes(key), start, end);
            Set<TypedTuple<V>> result = new LinkedHashSet<TypedTuple<V>>();
            for (Tuple t : set) {
                result.add(new DefaultTypedTuple<V>(bytes2Object(t.getValue(),
                    classNm), t.getScore()));
            }
            return result;
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<V> zReverseRange(K key, long start, long end,
                                       Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            Set<byte[]> byteset = driver.zRevRange(object2Bytes(key), start, end);
            Set<V> set = new LinkedHashSet<V>();
            for (byte[] bytes : byteset) {
                set.add(bytes2Object(bytes, classNm));
            }
            return set;
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<TypedTuple<V>> zReverseRangeWithScore(K key, long start,
                                                            long end, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            Set<Tuple> set = driver.zRevRangeWithScores(object2Bytes(key), start, end);
            Set<TypedTuple<V>> result = new LinkedHashSet<TypedTuple<V>>();
            for (Tuple t : set) {
                result.add(new DefaultTypedTuple<V>(bytes2Object(t.getValue(),
                    classNm), t.getScore()));
            }
            return result;
        } finally {
            driver.close();
        }
    }

    public <K, V> boolean zDelete(K key, V val) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.zRem(object2Bytes(key), object2Bytes(val));
        } finally {
            driver.close();
        }
    }

    public <K> Long zRemRangeByRank(K key, long start, long end) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.zRemRange(object2Bytes(key), start, end);
        } finally {
            driver.close();
        }
    }

    public <K> Long zRemRangeByScore(K key, double min, double max) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.zRemRangeByScore(object2Bytes(key), min, max);
        } finally {
            driver.close();
        }
    }

    public <K, V> Long lPush(K key, V... val) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.lPush(object2Bytes(key), objArray2BytesArray(val));
        } finally {
            driver.close();
        }
    }

    public <K, V> V lPop(K key, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            byte[] result = driver.lPop(object2Bytes(key));
            return bytes2Object(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V> V blPop(K key, int timeout, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            byte[] result = driver.bLPop(timeout, object2Bytes(key));
            return bytes2Object(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V> Long rPush(K key, V... val) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.rPush(object2Bytes(key), objArray2BytesArray(val));
        } finally {
            driver.close();
        }
    }

    public <K, V> V rPop(K key, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            byte[] result = driver.rPop(object2Bytes(key));
            return bytes2Object(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V> V brPop(K key, int timeout, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            byte[] result = driver.bRPop(timeout, object2Bytes(key));
            return bytes2Object(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K> Long lSize(K key) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.lLen(object2Bytes(key));
        } finally {
            driver.close();
        }
    }

    public <K, V> List<V> lGetRange(K key, long start, long end,
                                    Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            List<byte[]> result = driver.lRange(object2Bytes(key), start, end);
            return bytesList2ObjList(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K> boolean lTrim(K key, long start, long end) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.lTrim(object2Bytes(key), start, end).equals("OK");
        } finally {
            driver.close();
        }
    }

    public <K, V> V lIndex(K key, long index, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            byte[] result = driver.lIndex(object2Bytes(key), index);
            return bytes2Object(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V> Long sBatchAdd(K key, V... val) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.sAdd(object2Bytes(key), objArray2BytesArray(val));
        } finally {
            driver.close();
        }
    }

    public <K, V> Long sAdd(K key, V val) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.sAdd(object2Bytes(key), object2Bytes(val));
        } finally {
            driver.close();
        }
    }

    public <K, V> Long sRem(K key, V val) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.sRem(object2Bytes(key), object2Bytes(val));
        } finally {
            driver.close();
        }
    }

    public <K, V> V sPop(K key, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            byte[] result = driver.sPop(object2Bytes(key));
            return bytes2Object(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<V> sMembers(K key, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            Set<byte[]> result = driver.sMembers(object2Bytes(key));
            Set<V> set = new LinkedHashSet<V>();
            for (byte[] bytes : result) {
                set.add(bytes2Object(bytes, classNm));
            }
            return set;
        } finally {
            driver.close();
        }
    }

    public <K, V> V sRandMember(K key, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            byte[] result = driver.sRandMember(object2Bytes(key));
            return bytes2Object(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V> Long sSize(K key) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.sCard(object2Bytes(key));
        } finally {
            driver.close();
        }
    }

    public <K, V> Boolean sismember(K key, V val) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.sIsMember(object2Bytes(key), object2Bytes(val));
        } finally {
            driver.close();
        }
    }

    public <K, V, F> List<V> hMget(K key, F[] fields, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            List<byte[]> result = driver.hMGet(object2Bytes(key), objArray2BytesArray(fields));
            return bytesList2ObjList(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V, F> boolean hMset(K key, Map<F, V> hash) {
        NaviShardJedisDriver driver = getDriver();
        try {
            Map<byte[], byte[]> map = new LinkedHashMap<byte[], byte[]>();
            for (F bytes : hash.keySet()) {
                map.put(object2Bytes(bytes), object2Bytes(hash.get(bytes)));
            }
            return driver.hMSet(object2Bytes(key), map).equals("OK");
        } finally {
            driver.close();
        }
    }

    public <K, V, F> Long hSet(K key, F field, V val) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.hSet(object2Bytes(key), object2Bytes(field), object2Bytes(val));
        } finally {
            driver.close();
        }
    }

    public <K, V, F> Long hSetNX(K key, F field, V val) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.hSetNX(object2Bytes(key), object2Bytes(field), object2Bytes(val));
        } finally {
            driver.close();
        }
    }

    public <K, V, F> V hGet(K key, F field, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            byte[] result = driver.hGet(object2Bytes(key), object2Bytes(field));
            return bytes2Object(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, F> Long hDel(K key, F... fields) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.hDel(object2Bytes(key), object2Bytes(fields[0]));
        } finally {
            driver.close();
        }
    }

    public <K> Long hLen(K key) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.hLen(object2Bytes(key));
        } finally {
            driver.close();
        }
    }

    public <K, F> Set<F> hKeys(K key, Class<F> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            Set<byte[]> result = driver.hKeys(object2Bytes(key));
            Set<F> set = new LinkedHashSet<F>();
            for (byte[] bytes : result) {
                set.add(bytes2Object(bytes, classNm));
            }
            return set;
        } finally {
            driver.close();
        }
    }

    public <K, V> List<V> hVals(K key, Class<V> classNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            List<byte[]> result = driver.hVals(object2Bytes(key));
            return bytesList2ObjList(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V, F> Map<F, V> hGetAll(K key, Class<F> fieldClassNm, Class<V> valueClassNm) {
        NaviShardJedisDriver driver = getDriver();
        try {
            Map<byte[], byte[]> result = driver.hGetAll(object2Bytes(key));
            if (result == null || result.size() == 0) {
                return null;
            }
            Map<F, V> map = new LinkedHashMap<F, V>();
            for (byte[] field : result.keySet()) {
                map.put(bytes2Object(field, fieldClassNm), bytes2Object(result.get(field), valueClassNm));
            }
            return map;
        } finally {
            driver.close();
        }
    }

    public <K, F> boolean hExists(K key, F field) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.hExists(object2Bytes(key), object2Bytes(field));
        } finally {
            driver.close();
        }
    }

    public <K, F> Long hIncrBy(K key, F field, long delta) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.hIncrBy(object2Bytes(key), object2Bytes(field), delta);
        } finally {
            driver.close();
        }
    }

    public <K, V> Long append(K key, V val) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.append(object2Bytes(key), object2Bytes(val));
        } finally {
            driver.close();
        }
    }

    public <K> Long ttl(K key) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.ttl(object2Bytes(key));
        } finally {
            driver.close();
        }
    }

    public <K> boolean setbit(K key, long offset, boolean val) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.setBit(object2Bytes(key), offset, val);
        } finally {
            driver.close();
        }
    }

    public <K> boolean getbit(K key, long offset) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.getBit(object2Bytes(key), offset);
        } finally {
            driver.close();
        }
    }

    public <K> INaviMultiRedis multi(K key) {
        NaviShardJedisDriver driver = getDriver();
        return new MultiRedis(driver, driver.multi(object2Bytes(key)), null);
    }

    public <K> INaviMultiRedis openPipeline(K key) {
        NaviShardJedisDriver driver = getDriver();
        return new MultiRedis(driver, null, driver.openPipeline(object2Bytes(key)));
    }

    public <K> Object eval(K key, String script, int keyCount,
                           String... params) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.eval(object2Bytes(key), object2Bytes(script), keyCount, objArray2BytesArray(params));
        } finally {
            driver.close();
        }
    }

    public <K> Object evalsha(K key, String sha, int keyCount, String... params) {
        NaviShardJedisDriver driver = getDriver();
        try {
            return driver.eval(object2Bytes(key), object2Bytes(sha), keyCount, objArray2BytesArray(params));
        } finally {
            driver.close();
        }
    }

    private class MultiRedis implements INaviMultiRedis {
        NaviShardJedisDriver driver;
        Transaction tran;
        Pipeline pipe;

        public MultiRedis(NaviShardJedisDriver driver, Transaction tran, Pipeline pipe) {
            this.driver = driver;
            this.tran = tran;
            this.pipe = pipe;
        }

        public Transaction getTransaction() {
            return tran;
        }

        public Pipeline getPipeline() {
            return pipe;
        }

        public void returnObject() {
            driver.close();
        }

    }

    public <K> List<List<K>> groupKey(Class<K> classNm, K... keys) {
        throw new UnsupportedOperationException();
    }


}
