package com.youku.java.navi.server.serviceobj;

import com.alibaba.fastjson.JSONObject;
import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.Resp;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.utils.AlibabaJsonSerializer;
import com.youku.java.navi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;

/**
 * navi下dto需继承该超类 该类提供辅助性工具
 */
@Slf4j
public abstract class AbstractNaviBaseDto<P> implements Cloneable, Serializable {

    private static final long serialVersionUID = -3983785947326217708L;

    private Integer _null_ = null;

    public AbstractNaviBaseDto() {
        // P 类型必须为Long或String
        Class type = this.getActualTypeClass();
        if (!type.equals(String.class) && !type.equals(Long.class)) {
            throw new NaviSystemException("db entity 'id' type must be String or Long", NaviError.SYSERROR);
        }
    }

    private Class getActualTypeClass() {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        return (Class) type.getActualTypeArguments()[0];
    }

    public static <T extends AbstractNaviBaseDto> T createNullInstance(Class<T> clazz) {
        try {
            T dto = clazz.newInstance();
            dto.setNull();
            return dto;
        } catch (InstantiationException | IllegalAccessException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    public boolean isNull() {
        return _null_ != null && (1 == _null_);
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
        JSONObject json = new JSONObject(true);
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
    public abstract P getId();

    public abstract void setId(P id);

    protected String getPrefix() {
        String prefix = null;
        DtoPrefix annotation = this.getClass().getAnnotation(DtoPrefix.class);
        if (null != annotation) {
            prefix = annotation.prefix();
        }

        if (StringUtils.isEmpty(prefix)) {
            prefix = this.getClass().getName();
        }

        return prefix;
    }
}
