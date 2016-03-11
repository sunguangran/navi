package com.youku.java.navi.engine.component;

import com.youku.java.navi.engine.core.INaviCache;
import com.youku.java.navi.engine.core.INaviMessageQueue;
import com.youku.java.navi.engine.redis.INaviMultiRedis;
import com.youku.java.navi.utils.AlibabaJsonSerializer;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class NaviRedisMessageQueue implements INaviMessageQueue {

    private INaviCache service;

    public NaviRedisMessageQueue(INaviCache service) {
        this.service = service;
    }

    public <T> boolean offer(String key, T... t) {
        service.rPush(key, t);
        return true;
    }

    public <T> T poll(String key, Class<T> classNm) {
        return service.lPop(key, classNm);
    }

    public <T> T remove(String key, Class<T> classNm) {
        T t = service.lPop(key, classNm);
        if (t == null) {
            throw new NoSuchElementException("The queue maybe empty!");
        }
        return t;
    }

    public <T> T poll(String key, long timeout, TimeUnit unit, Class<T> classNm) {
        return service.blPop(key, (int) unit.toSeconds(timeout), classNm);
    }

    public <T> T poll(String key, long timeout, Class<T> classNm) {
        return service.blPop(key, (int) timeout, classNm);
    }

    public <T> T take(String key, Class<T> classNm) {
        return poll(key, 5, TimeUnit.MINUTES, classNm);
    }

    public <T> int drainTo(String key, Collection<T> c, int maxElements, Class<T> classNm) {
        INaviMultiRedis multi = service.multi(key);
        try {
            Transaction tran = multi.getTransaction();
            Response<List<byte[]>> response = tran.lrange(key.getBytes(), 0, maxElements - 1);
            tran.ltrim(key, maxElements, -1);
            tran.exec();
            List<byte[]> results = response.get();
            AlibabaJsonSerializer jsonSerializer = new AlibabaJsonSerializer();
            for (byte[] result : results) {
                c.add(jsonSerializer.getObjectFromBytes(result, classNm));
            }
            return c.size();
        } finally {
            multi.returnObject();
        }
    }

    public long size(String key) {
        return service.lSize(key);
    }

    public boolean isEmpty(String key) {
        return size(key) == 0;
    }

    public <T> T peek(String key, Class<T> classNm) {
        List<T> list = service.lGetRange(key, 0, 1, classNm);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public <T> T element(String key, Class<T> classNm) {
        List<T> list = service.lGetRange(key, 0, 1, classNm);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            throw new NoSuchElementException("The queue maybe empty!");
        }
    }

}
