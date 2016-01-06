package com.cuckoo.framework.navi.engine.datasource.driver;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.DefaultTuple;
import org.springframework.data.redis.connection.RedisListCommands.Position;
import org.springframework.data.redis.connection.RedisZSetCommands.Tuple;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.connection.SortParameters.Order;
import org.springframework.data.redis.connection.SortParameters.Range;
import org.springframework.util.Assert;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Set;

public class NaviShardedJedisUtils {
    private static final byte[] ONE = new byte[]{1};
    private static final byte[] ZERO = new byte[]{0};

    public static SortingParams convertSortParams(SortParameters params) {
        SortingParams jedisParams = null;

        if (params != null) {
            jedisParams = new SortingParams();

            byte[] byPattern = params.getByPattern();
            if (byPattern != null) {
                jedisParams.by(params.getByPattern());
            }

            byte[][] getPattern = params.getGetPattern();
            if (getPattern != null) {
                jedisParams.get(getPattern);
            }

            Range limit = params.getLimit();
            if (limit != null) {
                jedisParams.limit((int) limit.getStart(),
                    (int) limit.getCount());
            }
            Order order = params.getOrder();
            if (order != null && order.equals(Order.DESC)) {
                jedisParams.desc();
            }
            Boolean isAlpha = params.isAlphabetic();
            if (isAlpha != null && isAlpha) {
                jedisParams.alpha();
            }
        }
        return jedisParams;
    }

    public static DataAccessException convertJedisAccessException(IOException ex) {
        if (ex instanceof UnknownHostException) {
            return new RedisConnectionFailureException("Unknown host " + ex.getMessage(), ex);
        }
        return new RedisConnectionFailureException("Could not connect to Redis server", ex);
    }

    public static DataAccessException convertJedisAccessException(JedisException ex, String host) {
        if (ex instanceof JedisDataException) {
            return new InvalidDataAccessApiUsageException(ex.getMessage() + " host:" + host, ex);
        }
        if (ex instanceof JedisConnectionException) {
            return new RedisConnectionFailureException(ex.getMessage() + " host:" + host, ex);
        }

        // fallback to invalid data exception
        return new InvalidDataAccessApiUsageException(ex.getMessage(), ex);
    }

    public static Boolean convertCodeReply(Number code) {
        return (code != null ? code.intValue() == 1 : null);
    }

    public static byte[] asBit(boolean value) {
        return (value ? ONE : ZERO);
    }

    public static LIST_POSITION convertPosition(Position where) {
        Assert.notNull("list positions are mandatory");
        return (Position.AFTER.equals(where) ? LIST_POSITION.AFTER : LIST_POSITION.BEFORE);
    }

    public static Set<Tuple> convertJedisTuple(Set<redis.clients.jedis.Tuple> tuples) {
        Set<Tuple> value = new LinkedHashSet<Tuple>(tuples.size());
        for (redis.clients.jedis.Tuple tuple : tuples) {
            value.add(new DefaultTuple(tuple.getBinaryElement(), tuple.getScore()));
        }

        return value;
    }

    public static DataAccessException convertJedisAccessException(RuntimeException ex) {
        if (ex instanceof JedisException) {
            return convertJedisAccessException((JedisException) ex);
        }

        return new RedisSystemException("Unknown exception", ex);
    }
}
