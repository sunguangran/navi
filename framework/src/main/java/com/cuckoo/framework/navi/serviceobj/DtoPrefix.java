package com.cuckoo.framework.navi.serviceobj;

import java.lang.annotation.*;

/**
 * 用于表示DTO前缀，。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface DtoPrefix {
    public String prefix() default "";
}
