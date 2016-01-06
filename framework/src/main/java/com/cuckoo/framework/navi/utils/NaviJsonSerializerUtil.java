package com.cuckoo.framework.navi.utils;

import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.Charset;

/**
 * @see AlibabaJsonSerializer
 * @deprecated 在版本升级过程中发现有内存泄露情况，谨慎使用
 */
public class NaviJsonSerializerUtil {

    public static <T> String serialize(T t) throws Exception {
        return JSON.toJSONString(t);
    }

    public static <T> T deSerialize(String jsonStr, Class<T> clazz)
        throws Exception {
        return JSON.parseObject(jsonStr, clazz);
    }

    public static <T> byte[] serialize_byte(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        } else if (t instanceof String) {
            return t.toString().getBytes(Charset.forName("UTF8"));
        }
        return JSON.toJSONBytes(t);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deSerialize(byte[] bytes, Class<T> classNm)
        throws SerializationException {
        if (bytes == null) {
            return null;
        } else if (String.class.equals(classNm)) {
            return (T) new String(bytes, Charset.forName("UTF8"));
        }
        return (T) JSON.parseObject(bytes, classNm);
    }
}
