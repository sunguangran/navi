package com.youku.java.navi.engine.component;

import com.youku.java.navi.engine.core.INaviCache;
import com.youku.java.navi.engine.core.INaviMessageQueue;
import com.youku.java.navi.engine.redis.INaviMultiRedis;
import com.youku.java.navi.utils.AlibabaJsonSerializer;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class NaviNewRedisMutiKeyMessageQueue implements INaviMessageQueue {

    private AlibabaJsonSerializer jsonSerializer = new AlibabaJsonSerializer();
    private INaviCache service;
    private String setKey;

    public NaviNewRedisMutiKeyMessageQueue(String setKey, INaviCache service) {
        this.setKey = setKey;
        this.service = service;
    }

    public <T> boolean offer(String key, T... t) {
        spushAndrpush(setKey, key, t);
        return true;
    }

    public <T> T poll(String key, Class<T> classNm) {
        if (StringUtils.isEmpty(key)) {
            key = setKey;
        }
        String listKey = service.sRandMember(key, String.class);
        if (StringUtils.isEmpty(listKey)) {
            return null;
        }
        T t = service.lPop(listKey, classNm);
        if (t == null) {
            removeKey(key, listKey);
        }
        return t;
    }

    public <T> T remove(String key, Class<T> classNm) {
        T t = poll(key, classNm);
        if (t == null) {
            throw new NoSuchElementException("The queue maybe empty!");
        }
        return t;
    }

    public <T> T poll(String key, long timeout, TimeUnit unit, Class<T> classNm) {
        return poll(key, (int) unit.toSeconds(timeout), classNm);
    }

    public <T> T poll(String key, long timeout, Class<T> classNm) {
        if (StringUtils.isEmpty(key)) {
            key = setKey;
        }
        String listKey = bspop(key, timeout);
        if (StringUtils.isEmpty(listKey)) {
            return null;
        }
        T t = service.lPop(listKey, classNm);
        if (t == null) {
            removeKey(key, listKey);
        }
        return t;
    }

    public <T> T take(String key, Class<T> classNm) {
        return poll(key, 5, TimeUnit.MINUTES, classNm);
    }

    public <T> int drainTo(String key, Collection<T> c, int maxElements, Class<T> classNm) {
        if (StringUtils.isEmpty(key)) {
            key = setKey;
        }
        String listKey = service.sPop(key, String.class);
        if (StringUtils.isEmpty(listKey)) {
            return 0;
        }
        INaviMultiRedis multi = service.multi(key);
        try {
            Transaction tran = multi.getTransaction();
            Response<List<byte[]>> response = tran.lrange(listKey.getBytes(), 0, maxElements - 1);
            tran.ltrim(listKey, maxElements, -1);
            tran.exec();
            List<byte[]> results = response.get();
            for (byte[] result : results) {
                c.add(jsonSerializer.getObjectFromBytes(result, classNm));
            }
            if (results.size() < maxElements) {
                removeKey(key, listKey);
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

    private <T> void spushAndrpush(String setKey, String key, T... ts) {
        String script = "for i,arg in ipairs(ARGV) do redis.call('RPUSH', KEYS[3],arg) end redis.call('SADD',KEYS[1],KEYS[3]) if redis.call('LLEN',KEYS[2]) < redis.call('SCARD',KEYS[1]) then return redis.call('LPUSH',KEYS[2],KEYS[2]) end";
        String help = setKey + ":help";
        String[] params = new String[ts.length + 3];
        int i = 0;
        params[i++] = setKey;
        params[i++] = help;
        params[i++] = key;
        for (T t : ts) {
            if (t instanceof String) {
                params[i++] = (String) t;
            } else {
                params[i++] = jsonSerializer.getJSONString(t);
            }
        }
        service.eval(key, script, 3, params);
    }

    private void removeKey(String setKey, String key) {
        String script = "if redis.call('LLEN',KEYS[2]) == 0 then return redis.call('SREM',KEYS[1],KEYS[2]) end";
        service.eval(setKey, script, 2, setKey, key);
    }

    private String bspop(String key, long timeout) {
        String listKey = service.sRandMember(key, String.class);
        if (listKey == null) {
            String help = setKey + ":help";
            service.blPop(help, (int) timeout, String.class);
            listKey = service.sRandMember(key, String.class);
        }
        return listKey;
    }

}
