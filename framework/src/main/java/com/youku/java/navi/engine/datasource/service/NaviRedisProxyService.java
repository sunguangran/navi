package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.core.INaviCache;
import com.youku.java.navi.engine.core.INaviDriver;
import com.youku.java.navi.engine.datasource.driver.NaviRedisProxyDriver;
import com.youku.java.navi.engine.redis.INaviMultiRedis;
import com.youku.java.navi.utils.AlibabaJsonSerializer;

import java.util.*;

/**
 */
public class NaviRedisProxyService extends AbstractNaviDataService implements
    INaviCache {

    private AlibabaJsonSerializer jsonSerializer = new AlibabaJsonSerializer();


    protected NaviRedisProxyDriver getDriver() {
        INaviDriver driver = dataSource.getHandle();
        if (driver instanceof NaviRedisProxyDriver) {
            return (NaviRedisProxyDriver) driver;
        }
        driver.close();
        throw new NaviSystemException("the driver is invalid!",
            NaviError.SYSERROR);
    }

    private <K> String object2String(K k) {
        if (k instanceof String) {
            return (String) k;
        }
        return jsonSerializer.getJSONString(k);
    }

    private <K> String[] objArray2StringArray(K[] keys) {
        String[] strs = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            strs[i] = object2String(keys[i]);
        }
        return strs;
    }

    private <K> K string2Object(String str, Class<K> classNm) {
        if (classNm.equals(String.class)) {
            return classNm.cast(str);
        }
        return jsonSerializer.getObjectFromJsonStr(str, classNm);
    }

    private <V> List<V> strList2ObjList(List<String> list, Class<V> classNm) {
        List<V> objList = new ArrayList<V>();
        for (String str : list) {
            objList.add(string2Object(str, classNm));
        }
        return objList;
    }

    public <K> boolean expire(K key, long timeout) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.expire(object2String(key), timeout);
        } finally {
            driver.close();
        }
    }

    public <K, V> boolean set(K key, V val) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.set(object2String(key), object2String(val));
        } finally {
            driver.close();
        }
    }

    public <K, V> Long setnx(K key, V val) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.setnx(object2String(key), object2String(val));
        } finally {
            driver.close();
        }
    }

    public <K, V> boolean setex(K key, V val, long timeout) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.setex(object2String(key), object2String(val), timeout);
        } finally {
            driver.close();
        }
    }

    public <K, V> V getSet(K key, V val, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            String str = driver.getSet(object2String(key), object2String(val));
            return string2Object(str, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V> V get(K key, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            String str = driver.get(object2String(key));
            return string2Object(str, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V> List<V> MGet(Class<V> classNm, K... keys) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            List<String> list = driver.MGet(objArray2StringArray(keys));
            return strList2ObjList(list, classNm);
        } finally {
            driver.close();
        }
    }

    public <K> Long incr(K key, long delta) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.incr(object2String(key), delta);
        } finally {
            driver.close();
        }
    }

    public <K> Long decr(K key, long delta) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.decr(object2String(key), delta);
        } finally {
            driver.close();
        }
    }

    public <K> boolean exists(K key) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.exists(object2String(key));
        } finally {
            driver.close();
        }
    }

    public <K> Long delete(K... keys) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.delete(objArray2StringArray(keys));
        } finally {
            driver.close();
        }
    }

    public <K, V> Long zBatchAdd(K key, Map<V, Double> map) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            List<String> vals = new LinkedList<String>();
            for (V v : map.keySet()) {
                vals.add(object2String(v));
                vals.add(map.get(v).toString());
            }
            return driver.zBatchAdd(object2String(key), vals);
        } finally {
            driver.close();
        }
    }

    public <K, V> Long zadd(K key, V val, double score) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.zadd(object2String(key), object2String(val), score);
        } finally {
            driver.close();
        }
    }

    public <K, V> Double zscore(K key, V val) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.zscore(object2String(key), object2String(val));
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<V> zRevRangeByScore(K key, double min, double max,
                                          long limit, long skip, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            List<String> strList = driver.zRevRangeByScore(object2String(key), min, max, limit, skip, false);
            List<V> objList = strList2ObjList(strList, classNm);
            return new LinkedHashSet<V>(objList);
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<TypedTuple<V>> zRevRangeByScoreWithScore(K key, double min,
                                                               double max, long limit, long skip, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            List<String> list = driver.zRevRangeByScore(object2String(key), min, max, limit, skip, true);
            Set<TypedTuple<V>> set = new LinkedHashSet<TypedTuple<V>>();
            for (int i = 0; i < list.size(); i = i + 2) {
                set.add(new DefaultTypedTuple<V>(string2Object(list.get(i + 1), classNm), Double.valueOf(list.get(i))));
            }
            return set;
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<V> zRangeByScore(K key, double min, double max,
                                       long limit, long skip, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            List<String> strList = driver.zRangeByScore(object2String(key), min, max, limit, skip, false);
            List<V> objList = strList2ObjList(strList, classNm);
            return new LinkedHashSet<V>(objList);
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<TypedTuple<V>> zRangeByScoreWithScore(K key, double min,
                                                            double max, long limit, long skip, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            List<String> list = driver.zRangeByScore(object2String(key), min, max, limit, skip, true);
            Set<TypedTuple<V>> set = new LinkedHashSet<TypedTuple<V>>();
            for (int i = 0; i < list.size(); i = i + 2) {
                set.add(new DefaultTypedTuple<V>(string2Object(list.get(i + 1), classNm), Double.valueOf(list.get(i))));
            }
            return set;
        } finally {
            driver.close();
        }
    }


    public <K> Long zCount(K key, double min, double max) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.zCount(object2String(key), min, max);
        } finally {
            driver.close();
        }
    }

    public <K> Long zSize(K key) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.zSize(object2String(key));
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<V> zRange(K key, long start, long end, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            List<String> strList = driver.zRange(object2String(key), start, end, false);
            List<V> objList = strList2ObjList(strList, classNm);
            return new LinkedHashSet<V>(objList);
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<TypedTuple<V>> zRangeWithScore(K key, long start, long end, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            List<String> list = driver.zRange(object2String(key), start, end, true);
            Set<TypedTuple<V>> set = new LinkedHashSet<TypedTuple<V>>();
            for (int i = 0; i < list.size(); i = i + 2) {
                set.add(new DefaultTypedTuple<V>(string2Object(list.get(i + 1), classNm), Double.valueOf(list.get(i))));
            }
            return set;
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<V> zReverseRange(K key, long start, long end,
                                       Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            List<String> strList = driver.zReverseRange(object2String(key), start, end, false);
            List<V> objList = strList2ObjList(strList, classNm);
            return new LinkedHashSet<V>(objList);
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<TypedTuple<V>> zReverseRangeWithScore(K key, long start, long end, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            List<String> list = driver.zReverseRange(object2String(key), start, end, true);
            Set<TypedTuple<V>> set = new LinkedHashSet<TypedTuple<V>>();
            for (int i = 0; i < list.size(); i = i + 2) {
                set.add(new DefaultTypedTuple<V>(string2Object(list.get(i + 1), classNm), Double.valueOf(list.get(i))));
            }
            return set;
        } finally {
            driver.close();
        }
    }

    public <K, V> boolean zDelete(K key, V val) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.zDelete(object2String(key), object2String(val));
        } finally {
            driver.close();
        }
    }

    public <K> Long zRemRangeByRank(K key, long start, long end) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.zRemRangeByRank(object2String(key), start, end);
        } finally {
            driver.close();
        }
    }

    public <K> Long zRemRangeByScore(K key, double min, double max) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.zRemRangeByScore(object2String(key), min, max);
        } finally {
            driver.close();
        }
    }

    public <K, V> Long lPush(K key, V... val) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.lPush(object2String(key), objArray2StringArray(val));
        } finally {
            driver.close();
        }
    }

    public <K, V> V lPop(K key, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            String result = driver.lPop(object2String(key));
            return string2Object(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V> V blPop(K key, int timeout, Class<V> classNm) {
        throw new UnsupportedOperationException();
    }

    public <K, V> Long rPush(K key, V... val) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.rPush(object2String(key), objArray2StringArray(val));
        } finally {
            driver.close();
        }
    }

    public <K, V> V rPop(K key, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            String result = driver.rPop(object2String(key));
            return string2Object(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V> V brPop(K key, int timeout, Class<V> classNm) {
        throw new UnsupportedOperationException();
    }

    public <K> Long lSize(K key) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.lSize(object2String(key));
        } finally {
            driver.close();
        }
    }

    public <K, V> List<V> lGetRange(K key, long start, long end,
                                    Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            List<String> result = driver.lGetRange(object2String(key), start, end);
            return strList2ObjList(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K> boolean lTrim(K key, long start, long end) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.lTrim(object2String(key), start, end);
        } finally {
            driver.close();
        }
    }

    public <K, V> V lIndex(K key, long index, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            String result = driver.lIndex(object2String(key), index);
            return string2Object(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V> Long sBatchAdd(K key, V... val) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.sBatchAdd(object2String(key), objArray2StringArray(val));
        } finally {
            driver.close();
        }
    }

    public <K, V> Long sAdd(K key, V val) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.sAdd(object2String(key), object2String(val));
        } finally {
            driver.close();
        }
    }

    public <K, V> Long sRem(K key, V val) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.sRem(object2String(key), object2String(val));
        } finally {
            driver.close();
        }
    }

    public <K, V> V sPop(K key, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            String result = driver.sPop(object2String(key));
            return string2Object(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V> Set<V> sMembers(K key, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            List<String> result = driver.sMembers(object2String(key));
            List<V> list = strList2ObjList(result, classNm);
            return new LinkedHashSet<V>(list);
        } finally {
            driver.close();
        }
    }

    public <K, V> V sRandMember(K key, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            String result = driver.sRandMember(object2String(key));
            return string2Object(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V> Long sSize(K key) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.sSize(object2String(key));
        } finally {
            driver.close();
        }
    }

    public <K, V> Boolean sismember(K key, V val) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.sismember(object2String(key), object2String(val));
        } finally {
            driver.close();
        }
    }

    public <K, V, F> List<V> hMget(K key, F[] fields, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            List<String> result = driver.hMget(object2String(key), objArray2StringArray(fields));
            return strList2ObjList(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V, F> boolean hMset(K key, Map<F, V> hash) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            Map<String, String> map = new LinkedHashMap<String, String>();
            for (F str : hash.keySet()) {
                map.put(object2String(str), object2String(hash.get(str)));
            }
            return driver.hMset(object2String(key), map);
        } finally {
            driver.close();
        }
    }

    public <K, V, F> Long hSet(K key, F field, V val) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.hSet(object2String(key), object2String(field), object2String(val));
        } finally {
            driver.close();
        }
    }

    public <K, V, F> Long hSetNX(K key, F field, V val) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.hSetNX(object2String(key), object2String(field), object2String(val));
        } finally {
            driver.close();
        }
    }

    public <K, V, F> V hGet(K key, F field, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            String result = driver.hGet(object2String(key), object2String(field));
            return string2Object(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, F> Long hDel(K key, F... fields) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.hDel(object2String(key), objArray2StringArray(fields));
        } finally {
            driver.close();
        }
    }

    public <K> Long hLen(K key) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.hLen(object2String(key));
        } finally {
            driver.close();
        }
    }

    public <K, F> Set<F> hKeys(K key, Class<F> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            List<String> result = driver.hKeys(object2String(key));
            return new LinkedHashSet<F>(strList2ObjList(result, classNm));
        } finally {
            driver.close();
        }
    }

    public <K, V> List<V> hVals(K key, Class<V> classNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            List<String> result = driver.hVals(object2String(key));
            return strList2ObjList(result, classNm);
        } finally {
            driver.close();
        }
    }

    public <K, V, F> Map<F, V> hGetAll(K key, Class<F> fieldClassNm, Class<V> valueClassNm) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            Map<String, String> result = driver.hGetAll(object2String(key));
            if (result == null || result.size() == 0) {
                return null;
            }
            Map<F, V> map = new LinkedHashMap<F, V>();
            for (String field : result.keySet()) {
                map.put(string2Object(field, fieldClassNm), string2Object(result.get(field), valueClassNm));
            }
            return map;
        } finally {
            driver.close();
        }
    }

    public <K, F> boolean hExists(K key, F field) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.hExists(object2String(key), object2String(field));
        } finally {
            driver.close();
        }
    }

    public <K, F> Long hIncrBy(K key, F field, long delta) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.hIncrBy(object2String(key), object2String(field), delta);
        } finally {
            driver.close();
        }
    }

    public <K, V> Long append(K key, V val) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.append(object2String(key), object2String(val));
        } finally {
            driver.close();
        }
    }

    public <K> Long ttl(K key) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.ttl(object2String(key));
        } finally {
            driver.close();
        }
    }

    public <K> boolean setbit(K key, long offset, boolean val) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.setbit(object2String(key), offset, val);
        } finally {
            driver.close();
        }
    }

    public <K> boolean getbit(K key, long offset) {
        NaviRedisProxyDriver driver = getDriver();
        try {
            return driver.getbit(object2String(key), offset);
        } finally {
            driver.close();
        }
    }

    public <K> INaviMultiRedis multi(K key) {
        throw new UnsupportedOperationException();
    }

    public <K> INaviMultiRedis openPipeline(K key) {
        throw new UnsupportedOperationException();
    }

    public <K> Object eval(K key, String script, int keyCount,
                           String... params) {
        throw new UnsupportedOperationException();
    }

    public <K> Object evalsha(K key, String sha, int keyCount, String... params) {
        throw new UnsupportedOperationException();
    }

    public <K> List<List<K>> groupKey(Class<K> classNm, K... keys) {
        throw new UnsupportedOperationException();
    }


}
