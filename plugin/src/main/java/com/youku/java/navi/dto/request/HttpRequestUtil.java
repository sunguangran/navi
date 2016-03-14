package com.youku.java.navi.dto.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youku.java.navi.common.exception.NaviBusinessException;
import com.youku.java.navi.server.api.NaviHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class HttpRequestUtil {

    public static <T> T createBeanFromRequest(NaviHttpRequest request, Class<? extends T> clazz) {
        try {
            T bean = clazz.newInstance();
            Field[] fields = bean.getClass().getDeclaredFields();

            for (Field field : fields) {
                if (request.getParameter(field.getName()) != null) {
                    String val = request.getParameter(field.getName());
                    if (val == null) {
                        continue;
                    }

                    Object obj = val;
                    if (field.getType().equals(JSONArray.class) || field.getType().equals(JSONObject.class)) {
                        obj = JSON.parse(val);
                    }

                    BeanUtils.setProperty(bean, field.getName(), obj);
                }
            }

            return bean;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    public static <T> T createBeanFromJSONObject(JSONObject object, Class<? extends T> clazz) {
        @SuppressWarnings("rawtypes")
        Set set = object.keySet();
        Map<Object, Object> jsonMap = new HashMap<Object, Object>();
        for (Object key : set) {
            jsonMap.put(key, object.get(String.valueOf(key)));
        }
        try {
            T bean = clazz.newInstance();
            Field[] fields = bean.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (jsonMap.get(field.getName()) != null) {
                    BeanUtils.setProperty(bean, field.getName(), jsonMap.get(field.getName()));
                }
            }
            return bean;
        } catch (Exception e) {
            throw new NaviBusinessException("invalid param detail", -100);
        }
    }

}