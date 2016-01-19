package com.cuckoo.framework.navi.serviceobj;

import com.cuckoo.framework.navi.common.annotation.BeanPrefix;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * navi下dto需继承该超类 该类提供辅助性工具
 */
public abstract class AbstractNaviBean implements Cloneable, Serializable {

    private static final long serialVersionUID = -806712930639356253L;

    public final void setValue(String field, Object value) throws InvocationTargetException, IllegalAccessException {
        if (value == null) {
            return;
        }

        BeanUtils.setProperty(this, field, value);
    }

    public final Object getValue(String fieldNm) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return BeanUtils.getProperty(this, fieldNm);
    }

    /**
     * 获取对象唯一标识
     */
    public abstract String getOId();

    protected String getPrefix() {
        BeanPrefix anatation = this.getClass().getAnnotation(BeanPrefix.class);

        String prefix = null;
        if (anatation != null) {
            prefix = anatation.value();
        }

        if (StringUtils.isEmpty(prefix)) {
            prefix = this.getClass().getName();
        }

        return prefix;
    }
}
