package com.cuckoo.framework.navi.serviceobj;

import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * navi下dto需继承该超类 该类提供辅助性工具
 */
public abstract class AbstractNaviDto implements Cloneable, Serializable {

    private static final long serialVersionUID = -3983785947326217708L;

    public AbstractNaviDto() {
    }

    public final void setValue(String fieldNm, Object value) throws InvocationTargetException, IllegalAccessException {
        if (value == null) {
            return;
        }

        BeanUtils.setProperty(this, fieldNm, value);
    }

    public final Object getValue(String fieldNm) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return BeanUtils.getProperty(this, fieldNm);
    }

    /**
     * 获取唯一标识对象的ID
     */
    public abstract String getOId();

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
