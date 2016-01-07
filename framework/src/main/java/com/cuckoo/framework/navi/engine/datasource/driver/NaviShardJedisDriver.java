package com.cuckoo.framework.navi.engine.datasource.driver;

import com.cuckoo.framework.navi.common.exception.NaviSystemException;
import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;
import com.cuckoo.framework.navi.utils.ServerUrlUtil.ServerUrl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisListCommands.Position;
import org.springframework.data.redis.connection.RedisZSetCommands.Aggregate;
import org.springframework.data.redis.connection.RedisZSetCommands.Tuple;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.connection.Subscription;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Hashing;

import java.io.IOException;
import java.util.*;

import static redis.clients.jedis.Protocol.toByteArray;

@Slf4j
public class NaviShardJedisDriver extends AbstractNaviDriver {

    private ShardedJedis shardJedis;
    private ServerUrl server;
    private NaviPoolConfig poolConfig;

    public NaviShardJedisDriver(ServerUrl server, String auth,
                                NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
        this.server = server;
        this.poolConfig = poolConfig;
        connect();
    }

    private void connect() {
        if (StringUtils.isEmpty(server.getHost())) {
            shardJedis = new ShardedJedis(constructShardInfo(server.getUrl(),
                poolConfig), Hashing.MURMUR_HASH, null);
        } else {
            shardJedis = new ShardedJedis(constructShardInfo(server.getHost(),
                server.getPort(), poolConfig), Hashing.MURMUR_HASH, null);
        }
    }

    private List<JedisShardInfo> constructShardInfo(String servers,
                                                    NaviPoolConfig poolConfig) {
        String[] hosts = servers.split(",");
        List<JedisShardInfo> list = new ArrayList<JedisShardInfo>();
        for (String host : hosts) {
            String[] hostPairs = host.split(":");
            JedisShardInfo shardInfo = new JedisShardInfo(hostPairs[0],
                Integer.valueOf(hostPairs[1]),
                poolConfig.getConnectTimeout());
            list.add(shardInfo);
        }
        return list;
    }

    private List<JedisShardInfo> constructShardInfo(String host, int port,
                                                    NaviPoolConfig poolConfig) {
        List<JedisShardInfo> list = new ArrayList<JedisShardInfo>();
        JedisShardInfo shardInfo = new JedisShardInfo(host, port, poolConfig.getConnectTimeout());
        list.add(shardInfo);
        return list;
    }

    public Boolean exists(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.exists(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.exists(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long del(byte[]... keys) {
        try {
            if (!isAlive()) {
                connect();
            }
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                connect();
            } else {
                throw convertJedisAccessException(e, null);
            }
        }
        try {
            long i = 0;
            for (byte[] key : keys) {
                long re = shardJedis.getShard(key).del(key);
                if (re == 1) {
                    i++;
                }
            }
            return i;
        } catch (Exception e) {
            throw convertJedisAccessException(e, null);
        }
    }

    public DataType type(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return DataType.fromCode(jedis.type(key));
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return DataType.fromCode(jedis.type(key));
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
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
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.expire(key, (int) seconds) == 1;
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.expire(key, (int) seconds) == 1;
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Boolean expireAt(byte[] key, long unixTime) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.expireAt(key, unixTime) == 1;
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.expireAt(key, unixTime) == 1;
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Boolean persist(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.persist(key) == 1;
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.persist(key) == 1;
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Boolean move(byte[] key, int dbIndex) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.move(key, dbIndex) == 1;
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.move(key, dbIndex) == 1;
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long ttl(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.ttl(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.ttl(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public List<byte[]> sort(byte[] key, SortParameters params) {
        SortingParams sortParams = NaviShardedJedisUtils
            .convertSortParams(params);
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return sortParams != null ? jedis.sort(key, sortParams) : jedis
                .sort(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return sortParams != null ? jedis.sort(key, sortParams)
                        : jedis.sort(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long sort(byte[] key, SortParameters params, byte[] storeKey) {
        SortingParams sortParams = NaviShardedJedisUtils
            .convertSortParams(params);
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return sortParams != null ? jedis.sort(key, sortParams, storeKey)
                : jedis.sort(key, storeKey);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return sortParams != null ? jedis.sort(key, sortParams,
                        storeKey) : jedis.sort(key, storeKey);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public byte[] get(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.get(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.get(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public byte[] getSet(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.getSet(key, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.getSet(key, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public List<byte[]> mGet(byte[]... keys) {
        if (keys == null || keys.length == 0) {
            return new ArrayList<byte[]>();
        }
        try {
            if (!isAlive()) {
                connect();
            }
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                connect();
            } else {
                throw convertJedisAccessException(e, null);
            }
        }
        try {
            Map<String, Jedis> jedisMap = new LinkedHashMap<String, Jedis>();
            Map<String, List<byte[]>> keysMap = new LinkedHashMap<String, List<byte[]>>();
            for (byte[] key : keys) {
                Jedis jedis = shardJedis.getShard(key);
                String host = jedis.getClient().getHost();
                jedisMap.put(host, jedis);
                if (!keysMap.containsKey(host)) {
                    keysMap.put(host, new ArrayList<byte[]>());
                }
                keysMap.get(host).add(key);
            }
            Iterator<String> iter = jedisMap.keySet().iterator();
            List<byte[]> rs = new ArrayList<byte[]>();
            while (iter.hasNext()) {
                String host = iter.next();
                List<byte[]> tmpList = jedisMap.get(host).mget(
                    keysMap.get(host).toArray(new byte[0][]));
                rs.addAll(tmpList);
            }
            return rs;
        } catch (Exception e) {
            throw convertJedisAccessException(e, null);
        }
    }

    public String set(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.set(key, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.set(key, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long setNX(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.setnx(key, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.setnx(key, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public String setEx(byte[] key, long seconds, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.setex(key, (int) seconds, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.setex(key, (int) seconds, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public void mSet(Map<byte[], byte[]> tuple) {
        throw new UnsupportedOperationException();

    }

    public void mSetNX(Map<byte[], byte[]> tuple) {
        throw new UnsupportedOperationException();

    }

    public Long incr(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.incr(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.incr(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long incrBy(byte[] key, long value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.incrBy(key, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.incrBy(key, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long decr(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.decr(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.decr(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long decrBy(byte[] key, long value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.decrBy(key, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.decrBy(key, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long append(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.append(key, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.append(key, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public byte[] getRange(byte[] key, long begin, long end) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.substr(key, (int) begin, (int) end);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.substr(key, (int) begin, (int) end);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long setRange(byte[] key, byte[] value, long offset) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.setrange(key, offset, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.setrange(key, offset, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Boolean getBit(byte[] key, long offset) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.getbit(key, offset);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.getbit(key, offset);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Boolean setBit(byte[] key, long offset, boolean value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.setbit(key, offset, toByteArray(value ? 1 : 0));
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis
                        .setbit(key, offset, toByteArray(value ? 1 : 0));
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long strLen(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.strlen(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.strlen(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long rPush(byte[] key, byte[]... values) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.rpush(key, values);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.rpush(key, values);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long lPush(byte[] key, byte[]... values) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.lpush(key, values);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.lpush(key, values);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long rPushX(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.lpush(key, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.lpush(key, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long lPushX(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.lpushx(key, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.lpushx(key, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long lLen(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.llen(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.llen(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public List<byte[]> lRange(byte[] key, long begin, long end) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.lrange(key, (int) begin, (int) end);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.lrange(key, (int) begin, (int) end);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public String lTrim(byte[] key, long begin, long end) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.ltrim(key, (int) begin, (int) end);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.ltrim(key, (int) begin, (int) end);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public byte[] lIndex(byte[] key, long index) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.lindex(key, (int) index);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.lindex(key, (int) index);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long lInsert(byte[] key, Position where, byte[] pivot, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.linsert(key,
                NaviShardedJedisUtils.convertPosition(where), pivot, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.linsert(key,
                        NaviShardedJedisUtils.convertPosition(where),
                        pivot, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public String lSet(byte[] key, long index, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.lset(key, (int) index, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.lset(key, (int) index, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long lRem(byte[] key, long count, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.lrem(key, (int) count, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.lrem(key, (int) count, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public byte[] lPop(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.lpop(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.lpop(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public byte[] rPop(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.rpop(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.rpop(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public byte[] bLPop(int timeout, byte[] key) {
        Jedis jedis = null;
        List<byte[]> list = null;
        try {
            jedis = shardJedis.getShard(key);
            list = jedis.blpop(timeout, key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    list = jedis.blpop(timeout, key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
        if (list == null || list.size() < 2) {
            return null;
        }
        return list.get(1);
    }

    public byte[] bRPop(int timeout, byte[] key) {
        Jedis jedis = null;
        List<byte[]> list = null;
        try {
            jedis = shardJedis.getShard(key);
            list = jedis.blpop(timeout, key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    list = jedis.brpop(timeout, key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
        if (list == null || list.size() < 2) {
            return null;
        }
        return list.get(1);
    }

    public byte[] rPopLPush(byte[] srcKey, byte[] dstKey) {
        throw new UnsupportedOperationException();
    }

    public byte[] bRPopLPush(int timeout, byte[] srcKey, byte[] dstKey) {
        throw new UnsupportedOperationException();
    }

    public Long sAdd(byte[] key, byte[]... value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.sadd(key, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.sadd(key, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long sRem(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.srem(key, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.srem(key, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public byte[] sPop(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.spop(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.spop(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Boolean sMove(byte[] srcKey, byte[] destKey, byte[] value) {
        throw new UnsupportedOperationException();
    }

    public Long sCard(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.scard(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.scard(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Boolean sIsMember(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.sismember(key, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.sismember(key, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
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
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.smembers(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.smembers(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public byte[] sRandMember(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.srandmember(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.srandmember(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long zAdd(byte[] key, double score, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.zadd(key, score, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.zadd(key, score, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long zBatchAdd(byte[] key, Map<byte[], Double> scoreMembers) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.zadd(key, scoreMembers);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.zadd(key, scoreMembers);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Boolean zRem(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return NaviShardedJedisUtils.convertCodeReply(jedis
                .zrem(key, value));
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return NaviShardedJedisUtils.convertCodeReply(jedis.zrem(
                        key, value));
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Double zIncrBy(byte[] key, double increment, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.zincrby(key, increment, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.zincrby(key, increment, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long zRank(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.zrank(key, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.zrank(key, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long zRevRank(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.zrevrank(key, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.zrevrank(key, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Set<byte[]> zRange(byte[] key, long begin, long end) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.zrange(key, (int) begin, (int) end);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.zrange(key, (int) begin, (int) end);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Set<Tuple> zRangeWithScores(byte[] key, long begin, long end) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return NaviShardedJedisUtils.convertJedisTuple(jedis
                .zrevrangeWithScores(key, (int) begin, (int) end));
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return NaviShardedJedisUtils.convertJedisTuple(jedis
                        .zrevrangeWithScores(key, (int) begin, (int) end));
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Set<byte[]> zRangeByScore(byte[] key, double min, double max) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.zrangeByScore(key, min, max);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.zrangeByScore(key, min, max);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Set<Tuple> zRangeByScoreWithScores(byte[] key, double min, double max) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return NaviShardedJedisUtils.convertJedisTuple(jedis
                .zrangeByScoreWithScores(key, min, max));
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return NaviShardedJedisUtils.convertJedisTuple(jedis
                        .zrangeByScoreWithScores(key, min, max));
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Set<byte[]> zRangeByScore(byte[] key, double min, double max,
                                     long offset, long count) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.zrangeByScore(key, min, max, (int) offset, (int) count);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.zrangeByScore(key, min, max, (int) offset, (int) count);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Set<Tuple> zRangeByScoreWithScores(byte[] key, double min,
                                              double max, long offset, long count) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return NaviShardedJedisUtils.convertJedisTuple(jedis
                .zrangeByScoreWithScores(key, min, max, (int) offset,
                    (int) count));
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return NaviShardedJedisUtils.convertJedisTuple(jedis
                        .zrangeByScoreWithScores(key, min, max,
                            (int) offset, (int) count));
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Set<byte[]> zRevRange(byte[] key, long begin, long end) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.zrevrange(key, (int) begin, (int) end);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.zrevrange(key, (int) begin, (int) end);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Set<Tuple> zRevRangeWithScores(byte[] key, long begin, long end) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return NaviShardedJedisUtils.convertJedisTuple(jedis
                .zrevrangeWithScores(key, (int) begin, (int) end));
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return NaviShardedJedisUtils.convertJedisTuple(jedis
                        .zrevrangeWithScores(key, (int) begin, (int) end));
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Set<byte[]> zRevRangeByScore(byte[] key, double min, double max) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.zrevrangeByScore(key, max, min);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.zrevrangeByScore(key, max, min);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Set<Tuple> zRevRangeByScoreWithScores(byte[] key, double min,
                                                 double max) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return NaviShardedJedisUtils.convertJedisTuple(jedis
                .zrevrangeByScoreWithScores(key, max, min));
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return NaviShardedJedisUtils.convertJedisTuple(jedis
                        .zrevrangeByScoreWithScores(key, max, min));
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Set<byte[]> zRevRangeByScore(byte[] key, double min, double max,
                                        long offset, long count) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.zrevrangeByScore(key, max, min, (int) offset,
                (int) count);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.zrevrangeByScore(key, max, min, (int) offset,
                        (int) count);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Set<Tuple> zRevRangeByScoreWithScores(byte[] key, double min,
                                                 double max, long offset, long count) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return NaviShardedJedisUtils.convertJedisTuple(jedis
                .zrevrangeByScoreWithScores(key, max, min, (int) offset,
                    (int) count));
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return NaviShardedJedisUtils.convertJedisTuple(jedis
                        .zrevrangeByScoreWithScores(key, max, min,
                            (int) offset, (int) count));
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long zCount(byte[] key, double min, double max) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.zcount(key, min, max);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.zcount(key, min, max);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long zCard(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.zcard(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.zcard(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Double zScore(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.zscore(key, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.zscore(key, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long zRemRange(byte[] key, long begin, long end) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.zremrangeByRank(key, (int) begin, (int) end);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.zremrangeByRank(key, (int) begin, (int) end);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long zRemRangeByScore(byte[] key, double min, double max) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.zremrangeByScore(key, min, max);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.zremrangeByScore(key, min, max);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
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
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.hset(key, field, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.hset(key, field, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long hSetNX(byte[] key, byte[] field, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.hsetnx(key, field, value);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.hsetnx(key, field, value);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public byte[] hGet(byte[] key, byte[] field) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.hget(key, field);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.hget(key, field);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public List<byte[]> hMGet(byte[] key, byte[]... fields) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.hmget(key, fields);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.hmget(key, fields);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public String hMSet(byte[] key, Map<byte[], byte[]> hashes) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.hmset(key, hashes);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.hmset(key, hashes);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long hIncrBy(byte[] key, byte[] field, long delta) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.hincrBy(key, field, delta);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.hincrBy(key, field, delta);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Boolean hExists(byte[] key, byte[] field) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.hexists(key, field);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.hexists(key, field);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long hDel(byte[] key, byte[] field) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.hdel(key, field);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.hdel(key, field);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Long hLen(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.hlen(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.hlen(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Set<byte[]> hKeys(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.hkeys(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.hkeys(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public List<byte[]> hVals(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.hvals(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.hvals(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Map<byte[], byte[]> hGetAll(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.hgetAll(key);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    connect();
                    jedis = shardJedis.getShard(key);
                    return jedis.hgetAll(key);
                } catch (Exception ex) {
                    throw convertJedisAccessException(ex, jedis);
                }
            } else {
                throw convertJedisAccessException(e, jedis);
            }
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
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

    public Object eval(byte[] key, byte[] script, int keyCount, byte[]... params) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.eval(script, keyCount, params);
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public Object evalsha(byte[] key, byte[] sha, int keyCount, byte[]... params) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.evalsha(sha, keyCount, params);
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public void openPipeline() {
        throw new UnsupportedOperationException();
    }

    /**
     * 根据key寻找连接节点操作
     *
     * @param key
     */
    public Pipeline openPipeline(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.pipelined();
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }

    public List<Object> closePipeline() {
        throw new UnsupportedOperationException(
            "is not be surpported in the distributed envriment!");
    }

    /**
     * @param key
     * @return
     */
    public Transaction multi(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = shardJedis.getShard(key);
            return jedis.multi();
        } catch (Exception ex) {
            throw convertJedisAccessException(ex, jedis);
        }
    }


    /**
     * 临时
     *
     * @param ex
     * @return
     */
    public DataAccessException convertJedisAccessException(Exception ex,
                                                           Jedis jedis) {
        StringBuffer server = new StringBuffer();
        if (jedis != null) {
            server.append(jedis.getClient().getHost());
            server.append(":");
            server.append(jedis.getClient().getPort());
        }
        if (ex instanceof JedisException) {
            // check connection flag
            if (ex instanceof JedisConnectionException) {
                setBroken(true);
            }
            return NaviShardedJedisUtils.convertJedisAccessException(
                (JedisException) ex, server.toString());
        }
        if (ex instanceof IOException) {
            return NaviShardedJedisUtils
                .convertJedisAccessException((IOException) ex);
        }

        return new RedisSystemException("Unknown jedis exception", ex);
    }

    public void destroy() throws NaviSystemException {
        Collection<Jedis> allShards = shardJedis.getAllShards();
        for (Jedis jedis : allShards) {
            if (jedis.isConnected()) {
                try {
                    try {
                        jedis.quit();
                    } catch (Exception e) {
                        // log.warn("one of redis instances is quited failly!",
                        // e);
                    }
                    jedis.disconnect();
                    log.info("redis instance is destoried successfully!");
                } catch (Exception e) {
                    log.warn("one of redis instances is disconnected failly!",
                        e);
                }
            }
        }
        shardJedis = null;
    }

    public boolean isAlive() throws NaviSystemException {
        Collection<Jedis> allShards = shardJedis.getAllShards();
        for (Jedis shard : allShards) {
            if (!shard.ping().equals("PONG")) {
                return false;
            }
        }
        return true;
    }

    public boolean open() {
        return true;
    }

}
