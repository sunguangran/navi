package com.youku.java.navi.common;

import com.youku.java.navi.engine.core.INaviCache;
import com.youku.java.navi.server.serviceobj.AbstractNaviBaseDto;
import com.youku.java.navi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class CacheComponent {

    public static <T extends AbstractNaviBaseDto> String getCacheKey(Class<T> clazz, String... ids) {
        String pre = null;
        if (clazz.isAnnotationPresent(CacheDocument.class)) {
            CacheDocument an = clazz.getAnnotation(CacheDocument.class);
            pre = an.key();
        }

        if (StringUtils.isEmpty(pre)) {
            pre = clazz.getName();
        }

        return pre + ":" + StringUtils.implode(ids, ":");
    }

    public static <T extends AbstractNaviBaseDto> String getMqKey(Class<T> clazz) {
        String mq = null;
        int rate = 15 * 60000;
        if (clazz.isAnnotationPresent(CacheDocument.class)) {
            CacheDocument an = clazz.getAnnotation(CacheDocument.class);
            mq = an.mq();
            rate = an.rate();
        }

        int interval = (int) (new Date().getTime() / rate);
        return mq + ":" + interval;
    }

    public static int getExpire(Class<? extends AbstractNaviBaseDto> clazz) {
        int expire = 60 * 60 * 24;
        if (clazz.isAnnotationPresent(CacheDocument.class)) {
            CacheDocument an = clazz.getAnnotation(CacheDocument.class);
            expire = an.expire();
        }

        return expire;
    }

    /**
     * 判断key是否存在于缓存中。之所以用ttl而不是exist，是因为exist会赶上过期临界时间点，造成错误
     *
     * @param cacheService
     * @param key
     * @return
     */
    public static boolean existsInCache(INaviCache cacheService, String key) {
        long ttl = 0;
        try {
            ttl = cacheService.ttl(key);
        } catch (Exception e) {
            log.error("cache error: " + e.getMessage(), e);
        }

        return ttl == -1 || ttl >= 5;
    }

}
