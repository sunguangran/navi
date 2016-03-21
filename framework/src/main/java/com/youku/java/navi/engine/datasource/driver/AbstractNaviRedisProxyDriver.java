package com.youku.java.navi.engine.datasource.driver;

import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.ServerUrlUtil;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import com.youku.java.navi.engine.datasource.pool.RedisProxyPoolConfig;
import com.youku.java.navi.engine.redis.CmdParam;
import com.youku.java.navi.engine.redis.CmdParamFactory;
import com.youku.java.navi.engine.redis.JavaRedisProxy;
import com.youku.java.navi.engine.redis.RedisProxyResult;
import com.youku.java.navi.engine.redis.exception.RedisProxyException;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class AbstractNaviRedisProxyDriver extends AbstractNaviDriver {

    private JavaRedisProxy proxy;

    public void destroy() {
        proxy.freeProxy();
    }

    private String serverGroup;

    public AbstractNaviRedisProxyDriver(ServerUrlUtil.ServerUrl server, String auth, NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
        this.serverGroup = server.getUrl();
        String path = null;
        if (poolConfig instanceof RedisProxyPoolConfig) {
            path = ((RedisProxyPoolConfig) poolConfig).getPath();
        }
        this.proxy = new JavaRedisProxy(path);
    }

    public boolean isAlive() {
        return true;
    }

    public boolean open() {
        return true;
    }


    public RedisProxyResult execmd(String serverGroup, String serverName, String host, int port, String cmd, String key, CmdParam... params) throws RedisProxyException {
        return proxy.execmd(serverGroup, serverName, host, port, cmd, key, params);
    }

    public String getKeyServerInfo(String key) {
        return proxy.getKeyServerInfo(serverGroup, key);
    }

    public String getGroupServerinfo() {
        return proxy.getGroupServerinfo(serverGroup);
    }

    public String getKeyServerNameInfo(String key) {
        return proxy.getKeyServerInfo(serverGroup, key);
    }

    public String getGroupServerNameInfo() {
        return proxy.getGroupServerNameInfo(serverGroup);
    }

    public int getGroupHashTypeInfo() {
        return proxy.getGroupHashTypeInfo(serverGroup);
    }

    public String getReplSetInfoRedisProxy(String srv_name) {
        return proxy.getReplSetInfoRedisProxy(serverGroup, srv_name);
    }

    public boolean expire(String key, long timeout) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            CmdParam param = CmdParamFactory.buildIntParam(timeout);
            RedisProxyResult rs = proxy.execmd(serverGroup, "EXPIRE", key, param);
            return returnBoolean(rs, "1");
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public boolean set(String key, String val) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            CmdParam param = CmdParamFactory.buildArrayParam(val);
            RedisProxyResult rs = proxy.execmd(serverGroup, "SET", key, param);
            return returnBoolean(rs, "OK");
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long setnx(String key, String val) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            CmdParam param = CmdParamFactory.buildArrayParam(val);
            RedisProxyResult rs = proxy.execmd(serverGroup, "SETNX", key, param);
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public boolean setex(String key, String val, long timeout) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            CmdParam param1 = CmdParamFactory.buildArrayParam(String.valueOf(timeout));
            CmdParam param2 = CmdParamFactory.buildArrayParam(val);
            RedisProxyResult rs = proxy.execmd(serverGroup, "SETEX", key, param1, param2);
            return returnBoolean(rs, "OK");
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public String getSet(String key, String val) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            CmdParam param = CmdParamFactory.buildArrayParam(val);
            RedisProxyResult rs = proxy.execmd(serverGroup, "GETSET", key, param);
            return returnString(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public String get(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "GET", key, new CmdParam[0]);
            return returnString(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public List<String> MGet(String... keys) {
        if (keys == null || keys.length == 0) {
            return null;
        }
        try {
            String key = keys[0];
            String[] values = keys.length > 1 ? Arrays.copyOfRange(keys, 1, keys.length) : new String[0];
            CmdParam[] params = CmdParamFactory.buildListParam(values);
            RedisProxyResult rs = proxy.execmd(serverGroup, "MGET", key, params);
            return returnList(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long incr(String key, long delta) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            if (delta == 1) {
                RedisProxyResult rs = proxy.execmd(serverGroup, "INCR", key, new CmdParam[0]);
                return returnLong(rs);
            } else {
                CmdParam param = CmdParamFactory.buildIntParam(delta);
                RedisProxyResult rs = proxy.execmd(serverGroup, "INCRBY", key, param);
                return returnLong(rs);
            }
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long decr(String key, long delta) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            if (delta == 1) {
                RedisProxyResult rs = proxy.execmd(serverGroup, "DECR", key, new CmdParam[0]);
                return returnLong(rs);
            } else {
                CmdParam param = CmdParamFactory.buildIntParam(delta);
                RedisProxyResult rs = proxy.execmd(serverGroup, "DECRBY", key, param);
                return returnLong(rs);
            }
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public boolean exists(String key) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "EXISTS", key, new CmdParam[0]);
            return returnBoolean(rs, "1");
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long delete(String... keys) {
        if (keys == null || keys.length == 0) {
            return null;
        }
        try {

            String key = keys[0];
            String[] values = keys.length > 1 ? Arrays.copyOfRange(keys, 1, keys.length) : new String[0];
            CmdParam[] params = CmdParamFactory.buildListParam(values);
            RedisProxyResult rs = proxy.execmd(serverGroup, "DEL", key, params);
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long zBatchAdd(String key, List<String> vals) {
        if (StringUtils.isEmpty(key) || vals == null || vals.isEmpty()) {
            return null;
        }
        try {
            CmdParam[] params = CmdParamFactory.buildListParam(vals.toArray(new String[0]));
            RedisProxyResult rs = proxy.execmd(serverGroup, "ZBATCHADD", key, params);
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long zadd(String key, String value, double score) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            CmdParam param1 = CmdParamFactory.buildArrayParam(value);
            CmdParam param2 = CmdParamFactory.buildArrayParam(String.valueOf(score));
            RedisProxyResult rs = proxy.execmd(serverGroup, "ZADD", key, param2, param1);
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Double zscore(String key, String value) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            CmdParam param = CmdParamFactory.buildArrayParam(value);
            RedisProxyResult rs = proxy.execmd(serverGroup, "ZSCORE", key, param);
            return returnDouble(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public List<String> zRevRangeByScore(String key, double min, double max, long limit, long skip, boolean withscore) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }

        try {
            List<CmdParam> params = new ArrayList<>();
            params.add(CmdParamFactory.buildArrayParam(String.valueOf(max)));
            params.add(CmdParamFactory.buildArrayParam(String.valueOf(min)));
            if (limit > 0) {
                params.add(CmdParamFactory.buildArrayParam("limit"));
                params.add(CmdParamFactory.buildArrayParam(String.valueOf(skip)));
                params.add(CmdParamFactory.buildArrayParam(String.valueOf(limit)));
            }
            if (withscore) {
                params.add(CmdParamFactory.buildArrayParam("withscores"));
            }
            RedisProxyResult rs = proxy.execmd(serverGroup, "ZREVRANGEBYSCORE", key, params.toArray(new CmdParam[0]));
            return returnList(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public List<String> zRangeByScore(String key, double min, double max, long limit, long skip, boolean withscore) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }

        try {
            List<CmdParam> params = new ArrayList<>();
            params.add(CmdParamFactory.buildArrayParam(String.valueOf(min)));
            params.add(CmdParamFactory.buildArrayParam(String.valueOf(max)));
            if (limit > 0) {
                params.add(CmdParamFactory.buildArrayParam("limit"));
                params.add(CmdParamFactory.buildArrayParam(String.valueOf(skip)));
                params.add(CmdParamFactory.buildArrayParam(String.valueOf(limit)));
            }
            if (withscore) {
                params.add(CmdParamFactory.buildArrayParam("withscores"));
            }
            RedisProxyResult rs = proxy.execmd(serverGroup, "ZRANGEBYSCORE", key, params.toArray(new CmdParam[0]));
            return returnList(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long zCount(String key, double min, double max) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            List<CmdParam> params = new ArrayList<>();
            params.add(CmdParamFactory.buildArrayParam(String.valueOf(min)));
            params.add(CmdParamFactory.buildArrayParam(String.valueOf(max)));
            RedisProxyResult rs = proxy.execmd(serverGroup, "ZCOUNT", key, params.toArray(new CmdParam[0]));
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long zSize(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "ZCARD", key, new CmdParam[0]);
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public List<String> zRange(String key, long start, long end, boolean withscore) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            List<CmdParam> params = new ArrayList<>();
            params.add(CmdParamFactory.buildIntParam(start));
            params.add(CmdParamFactory.buildIntParam(end));
            if (withscore) {
                params.add(CmdParamFactory.buildArrayParam("withscores"));
            }
            RedisProxyResult rs = proxy.execmd(serverGroup, "ZRANGE", key, params.toArray(new CmdParam[0]));
            return returnList(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public List<String> zReverseRange(String key, long start, long end,
                                      boolean withscore) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            List<CmdParam> params = new ArrayList<>();
            params.add(CmdParamFactory.buildIntParam(start));
            params.add(CmdParamFactory.buildIntParam(end));
            if (withscore) {
                params.add(CmdParamFactory.buildCONSTRParam("withscores"));
            }
            RedisProxyResult rs = proxy.execmd(serverGroup, "ZREVRANGE", key, params.toArray(new CmdParam[0]));
            return returnList(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public boolean zDelete(String key, String... values) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            CmdParam[] params = CmdParamFactory.buildListParam(values);
            RedisProxyResult rs = proxy.execmd(serverGroup, "ZREM", key, params);
            return returnBoolean(rs, "1");
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long zRemRangeByRank(String key, long start, long end) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            List<CmdParam> params = new ArrayList<>();
            params.add(CmdParamFactory.buildArrayParam(String.valueOf(start)));
            params.add(CmdParamFactory.buildArrayParam(String.valueOf(end)));
            RedisProxyResult rs = proxy.execmd(serverGroup, "ZREMRANGEBYRANK", key, params.toArray(new CmdParam[0]));
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long zRemRangeByScore(String key, double min, double max) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            List<CmdParam> params = new ArrayList<>();
            params.add(CmdParamFactory.buildArrayParam(String.valueOf(min)));
            params.add(CmdParamFactory.buildArrayParam(String.valueOf(max)));
            RedisProxyResult rs = proxy.execmd(serverGroup, "ZREMRANGEBYSCORE", key, params.toArray(new CmdParam[0]));
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long lPush(String key, String... vals) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            CmdParam[] params = CmdParamFactory.buildListParam(vals);
            RedisProxyResult rs = proxy.execmd(serverGroup, "LPUSH", key, params);
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public String lPop(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "LPOP", key, new CmdParam[0]);
            return returnString(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }


    public Long rPush(String key, String... val) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            CmdParam[] params = CmdParamFactory.buildListParam(val);
            RedisProxyResult rs = proxy.execmd(serverGroup, "RPUSH", key, params);
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public String rPop(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "RPOP", key, new CmdParam[0]);
            return returnString(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }


    public Long lSize(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "LLEN", key, new CmdParam[0]);
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public List<String> lGetRange(String key, long start, long end) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            List<CmdParam> params = new ArrayList<CmdParam>();
            params.add(CmdParamFactory.buildIntParam(start));
            params.add(CmdParamFactory.buildIntParam(end));
            RedisProxyResult rs = proxy.execmd(serverGroup, "LRANGE", key, params.toArray(new CmdParam[0]));
            return returnList(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public boolean lTrim(String key, long start, long end) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            List<CmdParam> params = new ArrayList<CmdParam>();
            params.add(CmdParamFactory.buildIntParam(start));
            params.add(CmdParamFactory.buildIntParam(end));
            RedisProxyResult rs = proxy.execmd(serverGroup, "LTRIM", key, params.toArray(new CmdParam[0]));
            return returnBoolean(rs, "OK");
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public String lIndex(String key, long index) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "LINDEX", key, CmdParamFactory.buildIntParam(index));
            return returnString(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long sBatchAdd(String key, String... val) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "SADD", key, CmdParamFactory.buildListParam(val));
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long sAdd(String key, String value) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "SADD", key, CmdParamFactory.buildArrayParam(value));
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long sRem(String key, String value) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "SREM", key, CmdParamFactory.buildArrayParam(value));
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public String sPop(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "SPOP", key, new CmdParam[0]);
            return returnString(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public List<String> sMembers(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "SMEMBERS", key, new CmdParam[0]);
            return returnList(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public String sRandMember(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "SRANDMEMBER", key, new CmdParam[0]);
            return returnString(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long sSize(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "SCARD", key, new CmdParam[0]);
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public boolean sismember(String key, String value) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "SREM", key, CmdParamFactory.buildArrayParam(value));
            return returnBoolean(rs, "1");
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public List<String> hMget(String key, String... fields) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        if (fields == null || fields.length == 0) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "HMGET", key, CmdParamFactory.buildListParam(fields));
            return returnList(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public boolean hMset(String key, Map<String, String> hash) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        if (hash == null || hash.isEmpty()) {
            return false;
        }
        try {
            List<CmdParam> params = new ArrayList<CmdParam>();
            for (String hashKey : hash.keySet()) {
                params.add(CmdParamFactory.buildArrayParam(hashKey));
                params.add(CmdParamFactory.buildArrayParam(hash.get(hashKey)));
            }
            RedisProxyResult rs = proxy.execmd(serverGroup, "HMSET", key, params.toArray(new CmdParam[0]));
            return returnBoolean(rs, "OK");
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long hSet(String key, String field, String value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
            return null;
        }
        try {
            CmdParam fieldParam = CmdParamFactory.buildArrayParam(field);
            CmdParam valueParam = CmdParamFactory.buildArrayParam(value);
            RedisProxyResult rs = proxy.execmd(serverGroup, "HSET", key, fieldParam, valueParam);
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long hSetNX(String key, String field, String value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
            return null;
        }
        try {
            CmdParam fieldParam = CmdParamFactory.buildArrayParam(field);
            CmdParam valueParam = CmdParamFactory.buildArrayParam(value);
            RedisProxyResult rs = proxy.execmd(serverGroup, "HSETNX", key, fieldParam, valueParam);
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public String hGet(String key, String field) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "HGET", key, CmdParamFactory.buildArrayParam(field));
            return returnString(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long hDel(String key, String... fields) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        if (fields == null || fields.length == 0) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "HDEL", key, CmdParamFactory.buildListParam(fields));
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long hLen(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "HLEN", key, new CmdParam[0]);
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public List<String> hKeys(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "HKEYS", key, new CmdParam[0]);
            return returnList(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public List<String> hVals(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "HVALS", key, new CmdParam[0]);
            return returnList(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Map<String, String> hGetAll(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "HGETALL", key, new CmdParam[0]);
            List<String> list = returnList(rs);
            if (list == null || list.isEmpty()) {
                return null;
            }
            Map<String, String> map = new LinkedHashMap<String, String>();
            for (int i = 0; i < list.size(); i = i + 2) {
                map.put(list.get(i), list.get(i + 1));
            }
            return map;
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public boolean hExists(String key, String field) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
            return false;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "HEXISTS", key, CmdParamFactory.buildArrayParam(field));
            return returnBoolean(rs, "1");
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long hIncrBy(String key, String field, long delta) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "HINCRBY", key,
                CmdParamFactory.buildArrayParam(field),
                CmdParamFactory.buildArrayParam(String.valueOf(delta)));
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long append(String key, String val) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "APPEND", key, CmdParamFactory.buildArrayParam(val));
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public Long ttl(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "TTL", key, new CmdParam[0]);
            return returnLong(rs);
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public boolean setbit(String key, long offset, boolean val) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "SETBIT", key,
                CmdParamFactory.buildArrayParam(String.valueOf(offset)),
                CmdParamFactory.buildArrayParam(val ? "1" : "0"));
            return returnBoolean(rs, "1");
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public boolean getbit(String key, long offset) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            RedisProxyResult rs = proxy.execmd(serverGroup, "GETBIT", key,
                CmdParamFactory.buildArrayParam(String.valueOf(offset)));
            return returnBoolean(rs, "1");
        } catch (Exception e) {
            throw new NaviSystemException(e.getMessage(), NaviError.SYSERROR, e);
        }
    }

    public void afterPropertiesSet() throws Exception {

    }


    private Long returnLong(RedisProxyResult rs) {
        return StringUtils.isEmpty(rs.getStringResult()) ? null : Long.valueOf(rs.getStringResult());
    }

    private List<String> returnList(RedisProxyResult rs) {
        return rs.getListResult();
    }

    private String returnString(RedisProxyResult rs) {
        return rs.getStringResult();
    }

    private boolean returnBoolean(RedisProxyResult rs, String compareKey) {
        return compareKey.equals(rs.getStringResult());
    }

    private Double returnDouble(RedisProxyResult rs) {
        return StringUtils.isEmpty(rs.getStringResult()) ? null : Double.valueOf(rs.getStringResult());
    }

}
