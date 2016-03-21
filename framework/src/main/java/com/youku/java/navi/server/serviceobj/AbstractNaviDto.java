package com.youku.java.navi.server.serviceobj;

import com.alibaba.fastjson.JSONObject;
import com.youku.java.navi.common.Resp;
import com.youku.java.navi.utils.AlibabaJsonSerializer;
import com.youku.java.navi.utils.StringUtils;
import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * navi下dto需继承该超类 该类提供辅助性工具
 */
public abstract class AbstractNaviDto implements Cloneable, Serializable {

    private static final long serialVersionUID = -3983785947326217708L;

    private int _null_ = 0;

    public AbstractNaviDto() {
    }

    public boolean isNull() {
        return (1 == _null_);
    }

    public void setNull() {
        _null_ = 1;
    }

    public void setValue(String key, Object value) throws InvocationTargetException, IllegalAccessException {
        if (value == null) {
            return;
        }

        BeanUtils.setProperty(this, key, value);
    }

    public Object getValue(String key) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return BeanUtils.getProperty(this, key);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        if (this.isNull()) {
            return null;
        }

        for (Field field : this.getClass().getDeclaredFields()) {
            Annotation[] ans = field.getDeclaredAnnotations();
            if (ans != null && ans.length != 0) {
                for (Annotation an : ans) {
                    if (an instanceof Resp) {
                        try {
                            field.setAccessible(true);
                            Object val = field.get(this);
                            if (val != null) {
                                String key = StringUtils.isNotEmpty(((Resp) an).value()) ? ((Resp) an).value() : field.getName();

                                if (((Resp) an).ip()) {
                                    json.put(key, StringUtils.longToIP((Long) val));
                                } else if (((Resp) an).encode()) {
                                    json.put(key, StringUtils.encode((Long) val));
                                } else {
                                    json.put(key, val);
                                }
                            }
                        } catch (IllegalAccessException e) {
                            // log.error(e.getMessage(), e);
                        }
                        break;
                    }
                }
            }
        }

        json.remove("_null_");

        return json;
    }

    @Override
    public String toString() {
        try {
            AlibabaJsonSerializer jsonSerializer = new AlibabaJsonSerializer();
            return new String(jsonSerializer.getJSONBytes(this), "utf-8");
        } catch (UnsupportedEncodingException ignored) {
            return null;
        }
    }

    /**
     * 获取唯一标识对象的ID
     */
    public abstract String getOId();

    public abstract void setOId(Long id);

    protected String getPrefix() {
        String prefix = null;
        DtoPrefix anatation = this.getClass().getAnnotation(DtoPrefix.class);
        if (null != anatation) {
            prefix = anatation.prefix();
        }
        if (null == prefix || "".equals(prefix)) {
            prefix = this.getClass().getName();
        }

        return prefix;
    }
}
