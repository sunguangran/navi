package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.core.INaviCache;
import com.youku.java.navi.engine.core.INaviDriver;
import com.youku.java.navi.engine.datasource.driver.AbstractNaviPoolJedisDriver;
import com.youku.java.navi.engine.redis.INaviMultiRedis;
import com.youku.java.navi.utils.AlibabaJsonSerializer;
import org.springframework.data.redis.connection.RedisZSetCommands.Tuple;

import java.util.*;

public class NaviPooledShardedJedisService extends AbstractNaviDataService implements INaviCache {

    private AlibabaJsonSerializer jsonSerializer = new AlibabaJsonSerializer();

    public AbstractNaviPoolJedisDriver getDriver() {
        INaviDriver driver = dataSource.getHandle();
        if (driver instanceof AbstractNaviPoolJedisDriver) {
            return (AbstractNaviPoolJedisDriver) driver;
        }

        throw new NaviSystemException("the driver is invalid!", NaviError.SYSERROR);
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
        return getDriver().expire(object2Bytes(key), timeout).booleanValue();
    }

    public <K, V> boolean set(K key, V val) {
        return getDriver().set(object2Bytes(key), object2Bytes(val)).equals("OK");
    }

    public <K, V> Long setnx(K key, V val) {
        return getDriver().setNX(object2Bytes(key), object2Bytes(val));
    }

    public <K, V> boolean setex(K key, V val, long timeout) {
        return getDriver().setEx(object2Bytes(key), timeout, object2Bytes(val)).equals("OK");
    }

    public <K, V> V getSet(K key, V val, Class<V> classNm) {
        byte[] bytes = getDriver().getSet(object2Bytes(key), object2Bytes(val));
        return bytes2Object(bytes, classNm);
    }

    public <K, V> V get(K key, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        byte[] bytes = driver.get(object2Bytes(key));
        return bytes2Object(bytes, classNm);
    }

    public <K, V> List<V> MGet(Class<V> classNm, K... keys) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        List<byte[]> list = driver.mGet(objArray2BytesArray(keys));
        return bytesList2ObjList(list, classNm);
    }

    public <K> Long incr(K key, long delta) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.incrBy(object2Bytes(key), delta);
    }

    public <K> Long decr(K key, long delta) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.decrBy(object2Bytes(key), delta);
    }

    public <K> boolean exists(K key) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.exists(object2Bytes(key));
    }

    public <K> Long delete(K... keys) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.del(objArray2BytesArray(keys));
    }

    public <K, V> Long zBatchAdd(K key, Map<V, Double> map) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        Map<byte[], Double> scoreMembers = new HashMap<>();
        for (V v : map.keySet()) {
            scoreMembers.put(object2Bytes(v), map.get(v));
        }
        return driver.zBatchAdd(object2Bytes(key), scoreMembers);
    }

    public <K, V> Long zadd(K key, V val, double score) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.zAdd(object2Bytes(key), score, object2Bytes(val));
    }

    public <K, V> Double zscore(K key, V val) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.zScore(object2Bytes(key), object2Bytes(val));
    }

    public <K, V> Set<V> zRevRangeByScore(K key, double min, double max, long limit, long skip, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        Set<byte[]> byteset = limit > 0 ? driver.zRevRangeByScore(object2Bytes(key), min, max, skip, limit) : driver.zRevRangeByScore(object2Bytes(key), min, max);
        Set<V> set = new LinkedHashSet<V>();
        for (byte[] bytes : byteset) {
            set.add(bytes2Object(bytes, classNm));
        }
        return set;
    }

    public <K, V> Set<TypedTuple<V>> zRevRangeByScoreWithScore(K key, double min, double max, long limit, long skip, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        Set<Tuple> set = limit > 0 ? driver.zRevRangeByScoreWithScores(
            object2Bytes(key), min, max, skip, limit) : driver.zRevRangeByScoreWithScores(object2Bytes(key), min, max
        );
        Set<TypedTuple<V>> result = new LinkedHashSet<>();
        for (Tuple t : set) {
            result.add(new DefaultTypedTuple<V>(bytes2Object(t.getValue(), classNm), t.getScore()));
        }
        return result;
    }

    public <K, V> Set<V> zRangeByScore(K key, double min, double max, long limit, long skip, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        Set<byte[]> byteset = limit > 0 ? driver.zRangeByScore(object2Bytes(key), min, max, skip, limit) : driver.zRangeByScore(object2Bytes(key), min, max);
        Set<V> set = new LinkedHashSet<>();
        for (byte[] bytes : byteset) {
            set.add(bytes2Object(bytes, classNm));
        }
        return set;
    }

    public <K, V> Set<TypedTuple<V>> zRangeByScoreWithScore(K key, double min, double max, long limit, long skip, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        Set<Tuple> set = limit > 0 ? driver.zRangeByScoreWithScores(
            object2Bytes(key), min, max, skip, limit) : driver.zRangeByScoreWithScores(object2Bytes(key), min, max
        );
        Set<TypedTuple<V>> result = new LinkedHashSet<>();
        for (Tuple t : set) {
            result.add(new DefaultTypedTuple<V>(bytes2Object(t.getValue(), classNm), t.getScore()));
        }
        return result;
    }

    public <K> Long zCount(K key, double min, double max) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.zCount(object2Bytes(key), min, max);
    }

    public <K> Long zSize(K key) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.zCard(object2Bytes(key));
    }

    public <K, V> Set<V> zRange(K key, long start, long end, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        Set<byte[]> byteset = driver.zRange(object2Bytes(key), start, end);
        Set<V> set = new LinkedHashSet<V>();
        for (byte[] bytes : byteset) {
            set.add(bytes2Object(bytes, classNm));
        }
        return set;
    }

    public <K, V> Set<TypedTuple<V>> zRangeWithScore(K key, long start, long end, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        Set<Tuple> set = driver.zRangeWithScores(object2Bytes(key), start, end);
        Set<TypedTuple<V>> result = new LinkedHashSet<>();
        for (Tuple t : set) {
            result.add(new DefaultTypedTuple<V>(bytes2Object(t.getValue(), classNm), t.getScore()));
        }
        return result;
    }

    public <K, V> Set<V> zReverseRange(K key, long start, long end, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        Set<byte[]> byteset = driver.zRevRange(object2Bytes(key), start, end);
        Set<V> set = new LinkedHashSet<>();
        for (byte[] bytes : byteset) {
            set.add(bytes2Object(bytes, classNm));
        }
        return set;
    }

    public <K, V> Set<TypedTuple<V>> zReverseRangeWithScore(K key, long start, long end, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();

        Set<Tuple> set = driver.zRevRangeWithScores(object2Bytes(key), start, end);
        Set<TypedTuple<V>> result = new LinkedHashSet<>();
        for (Tuple t : set) {
            result.add(new DefaultTypedTuple<V>(bytes2Object(t.getValue(), classNm), t.getScore()));
        }
        return result;
    }

    public <K, V> boolean zDelete(K key, V val) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.zRem(object2Bytes(key), object2Bytes(val));
    }

    public <K> Long zRemRangeByRank(K key, long start, long end) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.zRemRange(object2Bytes(key), start, end);
    }

    public <K> Long zRemRangeByScore(K key, double min, double max) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.zRemRangeByScore(object2Bytes(key), min, max);
    }

    public <K, V> Long lPush(K key, V... val) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.lPush(object2Bytes(key), objArray2BytesArray(val));
    }

    public <K, V> V lPop(K key, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        byte[] result = driver.lPop(object2Bytes(key));
        return bytes2Object(result, classNm);
    }

    public <K, V> V blPop(K key, int timeout, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        byte[] result = driver.bLPop(timeout, object2Bytes(key));
        return bytes2Object(result, classNm);
    }

    public <K, V> Long rPush(K key, V... val) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.rPush(object2Bytes(key), objArray2BytesArray(val));
    }

    public <K, V> V rPop(K key, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        byte[] result = driver.rPop(object2Bytes(key));
        return bytes2Object(result, classNm);
    }

    public <K, V> V brPop(K key, int timeout, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        byte[] result = driver.bRPop(timeout, object2Bytes(key));
        return bytes2Object(result, classNm);
    }

    public <K> Long lSize(K key) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.lLen(object2Bytes(key));
    }

    public <K, V> List<V> lGetRange(K key, long start, long end,
                                    Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        List<byte[]> result = driver.lRange(object2Bytes(key), start, end);
        return bytesList2ObjList(result, classNm);
    }

    public <K> boolean lTrim(K key, long start, long end) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.lTrim(object2Bytes(key), start, end).equals("OK");
    }

    public <K, V> V lIndex(K key, long index, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        byte[] result = driver.lIndex(object2Bytes(key), index);
        return bytes2Object(result, classNm);
    }

    public <K, V> Long sBatchAdd(K key, V... val) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.sAdd(object2Bytes(key), objArray2BytesArray(val));
    }

    public <K, V> Long sAdd(K key, V val) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.sAdd(object2Bytes(key), object2Bytes(val));
    }

    public <K, V> Long sRem(K key, V val) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.sRem(object2Bytes(key), object2Bytes(val));
    }

    public <K, V> V sPop(K key, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        byte[] result = driver.sPop(object2Bytes(key));
        return bytes2Object(result, classNm);
    }

    public <K, V> Set<V> sMembers(K key, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        Set<byte[]> result = driver.sMembers(object2Bytes(key));
        Set<V> set = new LinkedHashSet<V>();
        for (byte[] bytes : result) {
            set.add(bytes2Object(bytes, classNm));
        }
        return set;
    }

    public <K, V> V sRandMember(K key, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        byte[] result = driver.sRandMember(object2Bytes(key));
        return bytes2Object(result, classNm);
    }

    public <K, V> Long sSize(K key) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.sCard(object2Bytes(key));
    }

    public <K, V> Boolean sismember(K key, V val) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.sIsMember(object2Bytes(key), object2Bytes(val));
    }

    public <K, V, F> List<V> hMget(K key, F[] fields, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        List<byte[]> result = driver.hMGet(object2Bytes(key),
            objArray2BytesArray(fields));
        return bytesList2ObjList(result, classNm);
    }

    public <K, V, F> boolean hMset(K key, Map<F, V> hash) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        Map<byte[], byte[]> map = new LinkedHashMap<byte[], byte[]>();
        for (F bytes : hash.keySet()) {
            map.put(object2Bytes(bytes), object2Bytes(hash.get(bytes)));
        }
        return driver.hMSet(object2Bytes(key), map).equals("OK");
    }

    public <K, V, F> Long hSet(K key, F field, V val) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.hSet(object2Bytes(key), object2Bytes(field),
            object2Bytes(val));
    }

    public <K, V, F> Long hSetNX(K key, F field, V val) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.hSetNX(object2Bytes(key), object2Bytes(field),
            object2Bytes(val));
    }

    public <K, V, F> V hGet(K key, F field, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        byte[] result = driver.hGet(object2Bytes(key), object2Bytes(field));
        return bytes2Object(result, classNm);
    }

    public <K, F> Long hDel(K key, F... fields) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.hDel(object2Bytes(key), object2Bytes(fields[0]));
    }

    public <K> Long hLen(K key) {
        AbstractNaviPoolJedisDriver driver = getDriver();

        return driver.hLen(object2Bytes(key));

    }

    public <K, F> Set<F> hKeys(K key, Class<F> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        Set<byte[]> result = driver.hKeys(object2Bytes(key));
        Set<F> set = new LinkedHashSet<F>();
        for (byte[] bytes : result) {
            set.add(bytes2Object(bytes, classNm));
        }
        return set;
    }

    public <K, V> List<V> hVals(K key, Class<V> classNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        List<byte[]> result = driver.hVals(object2Bytes(key));
        return bytesList2ObjList(result, classNm);
    }

    public <K, V, F> Map<F, V> hGetAll(K key, Class<F> fieldClassNm,
                                       Class<V> valueClassNm) {
        AbstractNaviPoolJedisDriver driver = getDriver();

        Map<byte[], byte[]> result = driver.hGetAll(object2Bytes(key));
        if (result == null || result.size() == 0) {
            return null;
        }
        Map<F, V> map = new LinkedHashMap<F, V>();
        for (byte[] field : result.keySet()) {
            map.put(bytes2Object(field, fieldClassNm),
                bytes2Object(result.get(field), valueClassNm));
        }
        return map;
    }

    public <K, F> boolean hExists(K key, F field) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.hExists(object2Bytes(key), object2Bytes(field));
    }

    public <K, F> Long hIncrBy(K key, F field, long delta) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.hIncrBy(object2Bytes(key), object2Bytes(field), delta);
    }

    public <K, V> Long append(K key, V val) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.append(object2Bytes(key), object2Bytes(val));
    }

    public <K> Long ttl(K key) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.ttl(object2Bytes(key));
    }

    public <K> boolean setbit(K key, long offset, boolean val) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.setBit(object2Bytes(key), offset, val);
    }

    public <K> boolean getbit(K key, long offset) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.getBit(object2Bytes(key), offset);
    }

    public <K> INaviMultiRedis multi(K key) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.multi(object2Bytes(key));
    }

    public <K> INaviMultiRedis openPipeline(K key) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.openPipeline(object2Bytes(key));
    }

    public <K> Object eval(K key, String script, int keyCount, String... params) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.eval(object2Bytes(key), object2Bytes(script), keyCount, objArray2BytesArray(params));
    }

    public <K> Object evalsha(K key, String sha, int keyCount, String... params) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        return driver.evalsha(object2Bytes(key), object2Bytes(sha), keyCount, objArray2BytesArray(params));

    }

    public <K> List<List<K>> groupKey(Class<K> classNm, K... keys) {
        AbstractNaviPoolJedisDriver driver = getDriver();
        List<List<byte[]>> groups = driver.groupKeys(objArray2BytesArray(keys));
        List<List<K>> results = new LinkedList<List<K>>();
        for (List<byte[]> group : groups) {
            results.add(bytesList2ObjList(group, classNm));
        }
        return results;
    }


}
