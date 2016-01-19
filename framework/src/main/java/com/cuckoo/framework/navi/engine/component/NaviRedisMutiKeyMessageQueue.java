package com.cuckoo.framework.navi.engine.component;

import com.cuckoo.framework.navi.engine.core.INaviCache;
import com.cuckoo.framework.navi.engine.core.INaviMessageQueue;
import com.cuckoo.framework.navi.engine.datasource.driver.ANaviJedisDriver;
import com.cuckoo.framework.navi.engine.redis.INaviMultiRedis;
import com.cuckoo.framework.navi.utils.AlibabaJsonSerializer;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class NaviRedisMutiKeyMessageQueue implements INaviMessageQueue {

    private AlibabaJsonSerializer jsonSerializer = new AlibabaJsonSerializer();
    private INaviCache service;
    private String setKey;

    public NaviRedisMutiKeyMessageQueue(String setKey, INaviCache service) {
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

        String listKey = service.sPop(key, String.class);
        if (StringUtils.isEmpty(listKey)) {
            return null;
        }

        T t = service.lPop(listKey, classNm);
        if (t != null) {
            Long size = service.lSize(listKey);
            if (size != null && size != 0) {
                spush(key, listKey);
            }
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
        String listKey = bspop(key, timeout, String.class);
        if (StringUtils.isEmpty(listKey)) {
            return null;
        }
        T t = service.lPop(listKey, classNm);
        if (t != null) {
            Long size = service.lSize(listKey);
            if (size != null && size != 0) {
                spush(key, listKey);
            }
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

            if (results.size() != 0) {
                Long size = service.lSize(listKey);
                if (size != null && size != 0) {
                    spush(key, listKey);
                }
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

    private <T> void spushAndrpush(String setKey, String key, T... t) {
        String script = "redis.call('SADD',KEYS[1],ARGV[1]) if redis.call('LLEN',KEYS[2]) < redis.call('SCARD',KEYS[1]) then return redis.call('LPUSH',KEYS[2],ARGV[2]) end";
        String help = setKey + ":help";
        INaviMultiRedis multi = service.openPipeline(setKey);
        try {

            Pipeline pipe = multi.getPipeline();
            pipe.rpush(object2Bytes(key), objArray2BytesArray(t));
            pipe.eval(script, 2, new String[]{setKey, help, key, "HelpKey"});
            pipe.sync();
        } finally {
            multi.returnObject();
        }
    }

    private <T> void spush(String setKey, String key) {
        String script = "redis.call('SADD',KEYS[1],ARGV[1]) if redis.call('LLEN',KEYS[2]) < redis.call('SCARD',KEYS[1]) then return redis.call('LPUSH',KEYS[2],ARGV[2]) end";
        String help = setKey + ":help";
        service.eval(setKey.getBytes(), script, 2, new String[]{setKey, help, key, "HelpKey"});
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

    private <T> T bspop(String key, long timeout, Class<T> classNm) {
        ANaviJedisDriver driver = (ANaviJedisDriver) service.getDataSource().getHandle();
        try {
            byte[] re = driver.bLPop((int) timeout, object2Bytes(key + ":help"));
            if (re != null) {
                return jsonSerializer.getObjectFromBytes(driver.sPop(object2Bytes(key)), classNm);
            } else {
                return null;
            }
        } finally {
            driver.close();
        }
    }


}
