package com.cuckoo.framework.navi.engine.redis;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Pool;
import redis.clients.util.ShardInfo;
import redis.clients.util.Sharded;

import java.util.*;

@Slf4j
public abstract class AbstractPoolBinaryShardedJedis<R, S extends ShardInfo<R>> extends Sharded<R, S> implements BinaryJedisCommands {

    public AbstractPoolBinaryShardedJedis(List<S> shards) {
        super(shards);
    }

    private void logHost(Jedis jedis) {
        log.error("Host:" + jedis.getClient().getHost() + ":" + jedis.getClient().getPort());
    }

    abstract public Pool<Jedis> getPool(byte[] key);

    public Object doCommand(IRedisCommand command, byte[] key, Object... args) {
        Pool<Jedis> pool = getPool(key);
        Jedis j = pool.getResource();
        boolean ok = true;
        try {
            return command.doCommand(j, key, args);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    j.disconnect();
                    return command.doCommand(j, key, args);
                } catch (RuntimeException ex) {
                    ok = false;
                    logHost(j);
                    throw ex;
                }
            } else {
                ok = false;
                logHost(j);
                throw e;
            }
        } catch (RuntimeException e) {
            ok = false;
            logHost(j);
            throw e;
        } finally {
            if (ok) {
                pool.returnResource(j);
            } else {
                pool.returnBrokenResource(j);
            }
        }
    }

    public Collection<byte[]> doCommand(
        IRedisCollectionCommand<byte[]> command, byte[] key, Object... args) {
        Pool<Jedis> pool = getPool(key);
        Jedis j = pool.getResource();
        boolean ok = true;
        try {
            return command.doCommand(j, key, args);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null || e.getCause() instanceof JedisConnectionException) {
                try {
                    j.disconnect();
                    return command.doCommand(j, key, args);
                } catch (RuntimeException ex) {
                    ok = false;
                    logHost(j);
                    throw ex;
                }
            } else {
                ok = false;
                logHost(j);
                throw e;
            }
        } catch (RuntimeException e) {
            ok = false;
            logHost(j);
            throw e;
        } finally {
            if (ok) {
                pool.returnResource(j);
            } else {
                pool.returnBrokenResource(j);
            }
        }
    }

    public Collection<Tuple> doTupleCommand(
        IRedisCollectionCommand<Tuple> command, byte[] key, Object... args) {
        Pool<Jedis> pool = getPool(key);
        Jedis j = pool.getResource();
        boolean ok = true;
        try {
            return command.doCommand(j, key, args);
        } catch (JedisConnectionException e) {
            if (e.getCause() == null
                || e.getCause() instanceof JedisConnectionException) {
                try {
                    j.disconnect();
                    return command.doCommand(j, key, args);
                } catch (RuntimeException ex) {
                    ok = false;
                    logHost(j);
                    throw ex;
                }
            } else {
                ok = false;
                logHost(j);
                throw e;
            }
        } catch (RuntimeException e) {
            ok = false;
            logHost(j);
            throw e;
        } finally {
            if (ok) {
                pool.returnResource(j);
            } else {
                pool.returnBrokenResource(j);
            }
        }
    }

    public String set(byte[] key, byte[] value) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.set(key, (byte[]) args[0]);
            }
        };
        return (String) doCommand(command, key, value);
    }

    public byte[] get(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.get(key);
            }
        };
        return (byte[]) doCommand(command, key);
    }

    public Boolean exists(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.exists(key);
            }
        };
        return (Boolean) doCommand(command, key);
    }

    public Long persist(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.persist(key);
            }
        };
        return (Long) doCommand(command, key);
    }

    public String type(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.type(key);
            }
        };
        return (String) doCommand(command, key);

    }

    public Long expire(byte[] key, int seconds) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.expire(key, (Integer) args[0]);
            }
        };
        return (Long) doCommand(command, key, seconds);
    }

    public Long expireAt(byte[] key, long unixTime) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.expireAt(key, (Long) args[0]);
            }
        };
        return (Long) doCommand(command, key, unixTime);
    }

    public Long ttl(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.ttl(key);
            }
        };
        return (Long) doCommand(command, key);
    }

    public Boolean setbit(byte[] key, long offset, boolean value) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.setbit(key, (Long) args[0], (Boolean) args[1]);
            }
        };
        return (Boolean) doCommand(command, key, offset, value);
    }

    public Boolean setbit(byte[] key, long offset, byte[] value) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.setbit(key, (Long) args[0], (byte[]) args[1]);
            }
        };
        return (Boolean) doCommand(command, key, offset, value);
    }

    public Boolean getbit(byte[] key, long offset) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.getbit(key, (Long) args[0]);
            }
        };
        return (Boolean) doCommand(command, key, offset);
    }

    public Long setrange(byte[] key, long offset, byte[] value) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.setrange(key, (Long) args[0], (byte[]) args[1]);
            }
        };
        return (Long) doCommand(command, key, offset, value);
    }

    public byte[] getrange(byte[] key, long startOffset, long endOffset) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.getrange(key, (Long) args[0], (Long) args[1]);
            }
        };
        return (byte[]) doCommand(command, key, startOffset, endOffset);
    }

    public byte[] getSet(byte[] key, byte[] value) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.getSet(key, (byte[]) args[0]);
            }
        };
        return (byte[]) doCommand(command, key, value);
    }

    public Long setnx(byte[] key, byte[] value) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.setnx(key, (byte[]) args[0]);
            }
        };
        return (Long) doCommand(command, key, value);
    }

    public String setex(byte[] key, int seconds, byte[] value) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.setex(key, (Integer) args[0], (byte[]) args[1]);
            }
        };
        return (String) doCommand(command, key, seconds, value);
    }

    public Long decrBy(byte[] key, long integer) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.decrBy(key, (Integer) args[0]);
            }
        };
        return (Long) doCommand(command, key, integer);
    }

    public Long decr(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.decr(key);
            }
        };
        return (Long) doCommand(command, key);
    }

    public Long incrBy(byte[] key, long integer) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.incrBy(key, (Long) args[0]);
            }
        };
        return (Long) doCommand(command, key, integer);
    }

    public Double incrByFloat(byte[] key, double value) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.incrByFloat(key, (Double) args[0]);
            }
        };
        return (Double) doCommand(command, key, value);
    }

    public Long incr(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.incr(key);
            }
        };
        return (Long) doCommand(command, key);
    }

    public Long append(byte[] key, byte[] value) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.append(key, (byte[]) args[0]);
            }
        };
        return (Long) doCommand(command, key, value);
    }

    public byte[] substr(byte[] key, int start, int end) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.substr(key, (Integer) args[0], (Integer) args[1]);
            }
        };
        return (byte[]) doCommand(command, key, start, end);
    }

    public Long hset(byte[] key, byte[] field, byte[] value) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.hset(key, (byte[]) args[0], (byte[]) args[1]);
            }
        };
        return (Long) doCommand(command, key, field, value);
    }

    public byte[] hget(byte[] key, byte[] field) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.hget(key, (byte[]) args[0]);
            }
        };
        return (byte[]) doCommand(command, key, field);
    }

    public Long hsetnx(byte[] key, byte[] field, byte[] value) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.hsetnx(key, (byte[]) args[0], (byte[]) args[1]);
            }
        };
        return (Long) doCommand(command, key, field, value);
    }

    public String hmset(byte[] key, Map<byte[], byte[]> hash) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                Map<byte[], byte[]> hash = new HashMap<byte[], byte[]>();
                for (int i = 0; i < args.length; i = i + 2) {
                    hash.put((byte[]) args[i], (byte[]) args[i + 1]);
                }
                return jedis.hmset(key, hash);
            }
        };

        if (hash == null || hash.size() == 0) {
            return null;
        }
        Object[] args = new byte[hash.size() * 2][];
        int i = 0;
        for (byte[] field : hash.keySet()) {
            args[i++] = field;
            args[i++] = hash.get(field);
        }
        return (String) doCommand(command, key, args);
    }

    public List<byte[]> hmget(byte[] key, byte[]... fields) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.hmget(key, (byte[][]) args);
            }
        };
        Object[] args = fields;
        return (List<byte[]>) doCommand(command, key, args);
    }

    public Long hincrBy(byte[] key, byte[] field, long value) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.hincrBy(key, (byte[]) args[0], (Long) args[1]);
            }
        };
        return (Long) doCommand(command, key, field, value);
    }

    public Double hincrByFloat(byte[] key, byte[] field, double value) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.hincrByFloat(key, (byte[]) args[0], (Double) args[1]);
            }
        };
        return (Double) doCommand(command, key, field, value);
    }

    public Boolean hexists(byte[] key, byte[] field) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.hexists(key, (byte[]) args[0]);
            }
        };
        return (Boolean) doCommand(command, key, field);
    }

    public Long hdel(byte[] key, byte[]... field) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.hdel(key, (byte[][]) args);
            }
        };
        Object[] args = field;
        return (Long) doCommand(command, key, args);
    }

    public Long hlen(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.hlen(key);
            }
        };
        return (Long) doCommand(command, key);
    }

    public Set<byte[]> hkeys(byte[] key) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.hkeys(key);
            }
        };
        return (Set<byte[]>) doCommand(command, key);
    }

    public Collection<byte[]> hvals(byte[] key) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.hvals(key);
            }
        };
        return doCommand(command, key);
    }

    @SuppressWarnings("unchecked")
    public Map<byte[], byte[]> hgetAll(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.hgetAll(key);
            }
        };
        return (Map<byte[], byte[]>) doCommand(command, key);
    }

    public Long rpush(byte[] key, byte[]... arg) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.rpush(key, (byte[][]) args);
            }
        };
        Object[] args = arg;
        return (Long) doCommand(command, key, args);
    }

    public Long lpush(byte[] key, byte[]... arg) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.lpush(key, (byte[][]) args);
            }
        };
        Object[] args = arg;
        return (Long) doCommand(command, key, args);
    }

    public Long llen(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.llen(key);
            }
        };
        return (Long) doCommand(command, key);
    }

    public List<byte[]> lrange(byte[] key, long start, long end) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.lrange(key, (Long) args[0], (Long) args[1]);
            }
        };
        return (List<byte[]>) doCommand(command, key, start, end);
    }

    public String ltrim(byte[] key, long start, long end) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.ltrim(key, (Long) args[0], (Long) args[1]);
            }
        };
        return (String) doCommand(command, key, start, end);
    }

    public byte[] lindex(byte[] key, long index) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.lindex(key, (Long) args[0]);
            }
        };
        return (byte[]) doCommand(command, key, index);
    }

    public String lset(byte[] key, long index, byte[] value) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.lset(key, (Long) args[0], (byte[]) args[1]);
            }
        };
        return (String) doCommand(command, key, index, value);
    }

    public Long lrem(byte[] key, long count, byte[] value) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.lrem(key, (Long) args[0], (byte[]) args[1]);
            }
        };
        return (Long) doCommand(command, key, count, value);
    }

    public byte[] lpop(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.lpop(key);
            }
        };
        return (byte[]) doCommand(command, key);
    }

    public byte[] rpop(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.rpop(key);
            }
        };
        return (byte[]) doCommand(command, key);
    }

    public Long sadd(byte[] key, byte[]... member) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.sadd(key, (byte[][]) args);
            }
        };
        Object[] args = member;
        return (Long) doCommand(command, key, args);
    }

    public Set<byte[]> smembers(byte[] key) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.smembers(key);
            }
        };
        return (Set<byte[]>) doCommand(command, key);
    }

    public Long srem(byte[] key, byte[]... member) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.srem(key, (byte[][]) args);
            }
        };
        Object[] args = member;
        return (Long) doCommand(command, key, args);
    }

    public byte[] spop(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.spop(key);
            }
        };
        return (byte[]) doCommand(command, key);
    }

    public Long scard(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.scard(key);
            }
        };
        return (Long) doCommand(command, key);
    }

    public Boolean sismember(byte[] key, byte[] member) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.sismember(key, (byte[]) args[0]);
            }
        };
        return (Boolean) doCommand(command, key, member);
    }

    public byte[] srandmember(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.srandmember(key);
            }
        };
        return (byte[]) doCommand(command, key);
    }

    public Long strlen(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.strlen(key);
            }
        };
        return (Long) doCommand(command, key);
    }

    public Long zadd(byte[] key, double score, byte[] member) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zadd(key, (Double) args[0], (byte[]) args[1]);
            }
        };
        return (Long) doCommand(command, key, score, member);
    }

    public Long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                Map<byte[], Double> scoreMembers = new HashMap<byte[], Double>();
                for (int i = 0; i < args.length; i = i + 2) {
                    scoreMembers.put((byte[]) args[i], (Double) args[i + 1]);
                }
                return jedis.zadd(key, scoreMembers);
            }
        };
        Object[] args = new Object[scoreMembers.size() * 2];
        int i = 0;
        for (byte[] field : scoreMembers.keySet()) {
            args[i++] = field;
            args[i++] = scoreMembers.get(field);
        }
        return (Long) doCommand(command, key, args);
    }

    public Set<byte[]> zrange(byte[] key, long start, long end) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrange(key, (Long) args[0], (Long) args[1]);
            }
        };
        return (Set<byte[]>) doCommand(command, key, start, end);
    }

    public Long zrem(byte[] key, byte[]... member) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrem(key, (byte[][]) args);
            }
        };
        Object[] args = member;
        return (Long) doCommand(command, key, args);
    }

    public Double zincrby(byte[] key, double score, byte[] member) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zincrby(key, (Double) args[0], (byte[]) args[1]);
            }
        };
        return (Double) doCommand(command, key, score, member);
    }

    public Long zrank(byte[] key, byte[] member) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrank(key, (byte[]) args[0]);
            }
        };
        return (Long) doCommand(command, key, member);
    }

    public Long zrevrank(byte[] key, byte[] member) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrevrank(key, (byte[]) args[0]);
            }
        };
        return (Long) doCommand(command, key, member);
    }

    public Set<byte[]> zrevrange(byte[] key, long start, long end) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrevrange(key, (Long) args[0], (Long) args[1]);
            }
        };
        return (Set<byte[]>) doCommand(command, key, start, end);
    }

    public Set<Tuple> zrangeWithScores(byte[] key, long start, long end) {
        IRedisCollectionCommand<Tuple> command = new IRedisCollectionCommand<Tuple>() {
            public Collection<Tuple> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrangeWithScores(key, (Long) args[0], (Long) args[1]);
            }
        };
        return (Set<Tuple>) doTupleCommand(command, key, start, end);
    }

    public Set<Tuple> zrevrangeWithScores(byte[] key, long start, long end) {
        IRedisCollectionCommand<Tuple> command = new IRedisCollectionCommand<Tuple>() {
            public Collection<Tuple> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrevrangeWithScores(key, (Long) args[0], (Long) args[1]);
            }
        };
        return (Set<Tuple>) doTupleCommand(command, key, start, end);
    }

    public Long zcard(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zcard(key);
            }
        };
        return (Long) doCommand(command, key);
    }

    public Double zscore(byte[] key, byte[] member) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zscore(key, (byte[]) args[0]);
            }
        };
        return (Double) doCommand(command, key, member);
    }

    public List<byte[]> sort(byte[] key) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.sort(key);
            }
        };
        return (List<byte[]>) doCommand(command, key);
    }

    public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.sort(key, (SortingParams) args[0]);
            }
        };
        return (List<byte[]>) doCommand(command, key, sortingParameters);
    }

    public Long zcount(byte[] key, double min, double max) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zcount(key, (Double) args[0], (Double) args[1]);
            }
        };
        return (Long) doCommand(command, key, min, max);
    }

    public Long zcount(byte[] key, byte[] min, byte[] max) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zcount(key, (byte[]) args[0], (byte[]) args[1]);
            }
        };
        return (Long) doCommand(command, key, min, max);
    }

    public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrangeByScore(key, (Double) args[0], (Double) args[1]);
            }
        };
        return (Set<byte[]>) doCommand(command, key, min, max);
    }

    public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrangeByScore(key, (byte[]) args[0], (byte[]) args[1]);
            }
        };
        return (Set<byte[]>) doCommand(command, key, min, max);
    }

    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrevrangeByScore(key, (Double) args[0], (Double) args[1]);
            }
        };
        return (Set<byte[]>) doCommand(command, key, max, min);
    }

    public Set<byte[]> zrangeByScore(byte[] key, double min, double max,
                                     int offset, int count) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrangeByScore(key, (Double) args[0], (Double) args[1], (Integer) args[2], (Integer) args[3]);
            }
        };
        return (Set<byte[]>) doCommand(command, key, min, max, offset, count);
    }

    public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrevrangeByScore(key, (byte[]) args[0], (byte[]) args[1]);
            }
        };
        return (Set<byte[]>) doCommand(command, key, max, min);
    }

    public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max,
                                     int offset, int count) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrangeByScore(key, (byte[]) args[0], (byte[]) args[1], (Integer) args[2], (Integer) args[3]);
            }
        };
        return (Set<byte[]>) doCommand(command, key, min, max, offset, count);
    }

    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min,
                                        int offset, int count) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrevrangeByScore(key, (Double) args[0], (Double) args[1], (Integer) args[2], (Integer) args[3]);
            }
        };
        return (Set<byte[]>) doCommand(command, key, max, min, offset, count);
    }

    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
        IRedisCollectionCommand<Tuple> command = new IRedisCollectionCommand<Tuple>() {
            public Collection<Tuple> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrangeByScoreWithScores(key, (Double) args[0], (Double) args[1]);
            }
        };
        return (Set<Tuple>) doTupleCommand(command, key, min, max);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max,
                                                 double min) {
        IRedisCollectionCommand<Tuple> command = new IRedisCollectionCommand<Tuple>() {
            public Collection<Tuple> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrevrangeByScoreWithScores(key, (Double) args[0], (Double) args[1]);
            }
        };
        return (Set<Tuple>) doTupleCommand(command, key, max, min);
    }

    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min,
                                              double max, int offset, int count) {
        IRedisCollectionCommand<Tuple> command = new IRedisCollectionCommand<Tuple>() {
            public Collection<Tuple> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrangeByScoreWithScores(key, (Double) args[0], (Double) args[1], (Integer) args[2], (Integer) args[3]);
            }
        };
        return (Set<Tuple>) doTupleCommand(command, key, min, max, offset, count);
    }

    public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min,
                                        int offset, int count) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrevrangeByScore(key, (Double) args[0], (Double) args[1], (Integer) args[2], (Integer) args[3]);
            }
        };
        return (Set<byte[]>) doCommand(command, key, max, min, offset, count);
    }

    public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
        IRedisCollectionCommand<Tuple> command = new IRedisCollectionCommand<Tuple>() {
            public Collection<Tuple> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrangeByScoreWithScores(key, (byte[]) args[0], (byte[]) args[1]);
            }
        };
        return (Set<Tuple>) doTupleCommand(command, key, min, max);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max,
                                                 byte[] min) {
        IRedisCollectionCommand<Tuple> command = new IRedisCollectionCommand<Tuple>() {
            public Collection<Tuple> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrevrangeByScoreWithScores(key, (byte[]) args[0], (byte[]) args[1]);
            }
        };
        return (Set<Tuple>) doTupleCommand(command, key, max, min);
    }

    public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min,
                                              byte[] max, int offset, int count) {
        IRedisCollectionCommand<Tuple> command = new IRedisCollectionCommand<Tuple>() {
            public Collection<Tuple> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrangeByScoreWithScores(key, (byte[]) args[0], (byte[]) args[1], (Integer) args[2], (Integer) args[3]);
            }
        };
        return (Set<Tuple>) doTupleCommand(command, key, min, max, offset, count);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max,
                                                 double min, int offset, int count) {
        IRedisCollectionCommand<Tuple> command = new IRedisCollectionCommand<Tuple>() {
            public Collection<Tuple> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrangeByScoreWithScores(key, (Double) args[0],
                    (Double) args[1], (Integer) args[2], (Integer) args[3]);
            }
        };
        return (Set<Tuple>) doTupleCommand(command, key, max, min, offset, count);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max,
                                                 byte[] min, int offset, int count) {
        IRedisCollectionCommand<Tuple> command = new IRedisCollectionCommand<Tuple>() {
            public Collection<Tuple> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zrevrangeByScoreWithScores(key, (byte[]) args[0], (byte[]) args[1], (Integer) args[2], (Integer) args[3]);
            }
        };
        return (Set<Tuple>) doTupleCommand(command, key, max, min, offset, count);
    }

    public Long zremrangeByRank(byte[] key, long start, long end) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zremrangeByRank(key, (Long) args[0], (Long) args[1]);
            }
        };
        return (Long) doCommand(command, key, start, end);
    }

    public Long zremrangeByScore(byte[] key, double start, double end) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zremrangeByScore(key, (Double) args[0], (Double) args[1]);
            }
        };
        return (Long) doCommand(command, key, start, end);
    }

    public Long zremrangeByScore(byte[] key, byte[] start, byte[] end) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.zremrangeByScore(key, (byte[]) args[0], (byte[]) args[1]);
            }
        };
        return (Long) doCommand(command, key, start, end);
    }

    public Long linsert(byte[] key, LIST_POSITION where, byte[] pivot,
                        byte[] value) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.linsert(key, (LIST_POSITION) args[0], (byte[]) args[1], (byte[]) args[2]);
            }
        };
        return (Long) doCommand(command, key, where, pivot, value);
    }

    public Long lpushx(byte[] key, byte[]... arg) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.lpushx(key, (byte[][]) args);
            }
        };
        Object[] args = arg;
        return (Long) doCommand(command, key, args);
    }

    public Long rpushx(byte[] key, byte[]... arg) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.rpushx(key, (byte[][]) args);
            }
        };
        Object[] args = arg;
        return (Long) doCommand(command, key, args);
    }

    public List<byte[]> blpop(byte[] arg) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.blpop(key);
            }
        };
        return (List<byte[]>) doCommand(command, arg);
    }

    public List<byte[]> blpop(byte[] key, int seconds) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.blpop((Integer) args[0], key);
            }
        };
        return (List<byte[]>) doCommand(command, key, seconds);
    }

    public List<byte[]> brpop(byte[] arg) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.brpop(key);
            }
        };
        return (List<byte[]>) doCommand(command, arg);
    }

    public List<byte[]> brpop(byte[] key, int seconds) {
        IRedisCollectionCommand<byte[]> command = new IRedisCollectionCommand<byte[]>() {
            public Collection<byte[]> doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.brpop((Integer) args[0], key);
            }
        };
        return (List<byte[]>) doCommand(command, key, seconds);
    }

    public Long del(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.del(key);
            }
        };
        return (Long) doCommand(command, key);
    }

    public byte[] echo(byte[] arg) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.echo(key);
            }
        };
        return (byte[]) doCommand(command, arg);
    }

    public Long move(byte[] key, int dbIndex) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.move(key, (Integer) args[0]);
            }
        };
        return (Long) doCommand(command, key, dbIndex);
    }

    public Long bitcount(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.bitcount(key);
            }
        };
        return (Long) doCommand(command, key);
    }

    public Long bitcount(byte[] key, long start, long end) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.bitcount(key, (Long) args[0], (Long) args[1]);
            }
        };
        return (Long) doCommand(command, key, start, end);
    }

    public Long pfadd(byte[] key, byte[]... elements) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.pfadd(key, (byte[][]) args);
            }
        };
        Object[] args = elements;
        return (Long) doCommand(command, key, args);
    }

    public long pfcount(byte[] key) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.pfcount(key);
            }
        };
        return (Long) doCommand(command, key);
    }

    public Object eval(byte[] key, byte[] script, int keyCount, byte[]... params) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.eval((byte[]) args[0], (Integer) args[1], (byte[][]) args[2]);
            }
        };
        return doCommand(command, key, script, keyCount, params);
    }

    public Object evalsha(byte[] key, byte[] sha, int keyCount, byte[]... params) {
        IRedisCommand command = new IRedisCommand() {
            public Object doCommand(Jedis jedis, byte[] key, Object... args) {
                return jedis.evalsha((byte[]) args[0], (Integer) args[1], (byte[][]) args[2]);
            }
        };
        return doCommand(command, key, sha, keyCount, params);
    }


    abstract public List<List<byte[]>> groupKeys(byte[][] keys);


    public INaviMultiRedis multi(byte[] key) {
        Pool<Jedis> pool = getPool(key);
        Jedis j = pool.getResource();
        Transaction tran = null;
        boolean ok = true;
        try {
            tran = j.multi();
        } catch (JedisConnectionException e) {
            if (e.getCause() == null
                || e.getCause() instanceof JedisConnectionException) {
                try {
                    j.disconnect();
                    tran = j.multi();
                } catch (RuntimeException ex) {
                    ok = false;
                    logHost(j);
                    throw ex;
                }
            } else {
                ok = false;
                logHost(j);
                throw e;
            }
        } catch (RuntimeException e) {
            ok = false;
            logHost(j);
            throw e;
        } finally {
            if (!ok) {
                pool.returnBrokenResource(j);
            }
        }

        if (tran == null) {
            return null;
        }

        return new NaviMultiRedis(pool, j, tran, null);
    }

    public INaviMultiRedis openPipeline(byte[] key) {

        Pool<Jedis> pool = getPool(key);
        Jedis j = pool.getResource();
        Pipeline pipe = null;
        boolean ok = true;
        try {
            if (j.ping().toUpperCase().equals("PONG")) {
                pipe = j.pipelined();
            }
        } catch (JedisConnectionException e) {
            if (e.getCause() == null
                || e.getCause() instanceof JedisConnectionException) {
                try {
                    j.disconnect();
                    pipe = j.pipelined();
                } catch (RuntimeException ex) {
                    ok = false;
                    logHost(j);
                    throw ex;
                }
            } else {
                ok = false;
                logHost(j);
                throw e;
            }
        } catch (RuntimeException e) {
            ok = false;
            logHost(j);
            throw e;
        } finally {
            if (!ok) {
                pool.returnBrokenResource(j);
            }
        }

        if (pipe == null) {
            return null;
        }

        return new NaviMultiRedis(pool, j, null, pipe);
    }

    private class NaviMultiRedis implements INaviMultiRedis {
        private Pool<Jedis> pool;
        private Jedis jedis;
        private Transaction tran;
        private Pipeline pipe;

        public NaviMultiRedis(Pool<Jedis> pool, Jedis jedis, Transaction tran, Pipeline pipe) {
            this.pool = pool;
            this.jedis = jedis;
            this.tran = tran;
            this.pipe = pipe;
        }

        public Transaction getTransaction() {
            return this.tran;
        }

        public Pipeline getPipeline() {
            return this.pipe;
        }

        public void returnObject() {
            pool.returnResource(jedis);
        }
    }

}
