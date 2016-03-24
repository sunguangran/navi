package com.youku.java.navi.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson.util.ThreadLocalCache;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class AlibabaJsonSerializer extends JSON {

    private SerializeConfig config = new SerializeConfig();
    private ParserConfig parseConfig = new ParserConfig();

    public <T> String getJSONString(T t) {
        return toJSONString(t, config);
    }

    public <T> byte[] getJSONBytes(T t) {
        if (t == null) {
            return new byte[0];
        } else if (t instanceof String) {
            return t.toString().getBytes(Charset.forName("UTF8"));
        }
        return toJSONBytes(t, config);
    }

    @SuppressWarnings("unchecked")
    public <T> T getObjectFromBytes(byte[] input, Class<T> clazz) {
        if (input == null) {
            return null;
        } else if (String.class.equals(clazz)) {
            return (T) new String(input, Charset.forName("UTF8"));
        }
        return toParseObject(input, 0, input.length, ThreadLocalCache.getUTF8Decoder(), clazz);
    }

    public <T> T getObjectFromJsonStr(String jsonStr, Class<T> clazz) {
        return parseObject(jsonStr, clazz, parseConfig, DEFAULT_PARSER_FEATURE, new Feature[0]);
    }

    private <T> T toParseObject(byte[] input, int off, int len, CharsetDecoder charsetDecoder, Type clazz) {
        charsetDecoder.reset();

        int scaleLength = (int) (len * (double) charsetDecoder.maxCharsPerByte());
        char[] chars = ThreadLocalCache.getChars(scaleLength);

        ByteBuffer byteBuf = ByteBuffer.wrap(input, off, len);
        CharBuffer charByte = CharBuffer.wrap(chars);
        IOUtils.decode(charsetDecoder, byteBuf, charByte);

        int position = charByte.position();

        if (chars.length == 0) {
            return null;
        }

        DefaultJSONParser parser = new DefaultJSONParser(chars, position, parseConfig, DEFAULT_PARSER_FEATURE);

        T value = parser.parseObject(clazz);
        parser.handleResovleTask(value);
        parser.close();

        return value;
    }

}
