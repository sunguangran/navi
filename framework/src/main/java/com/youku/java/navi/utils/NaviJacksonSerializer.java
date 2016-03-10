package com.youku.java.navi.utils;

import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class NaviJacksonSerializer<T> implements RedisSerializer<T> {

    private Class<T> classNm;

    public NaviJacksonSerializer(Class<T> classNm) {
        this.classNm = classNm;
    }

    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        return JSON.toJSONBytes(t);
    }

    @SuppressWarnings("unchecked")
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }
        return (T) JSON.parseObject(bytes, classNm);
    }

}
