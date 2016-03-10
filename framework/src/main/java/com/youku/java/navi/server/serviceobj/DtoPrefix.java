package com.youku.java.navi.server.serviceobj;

import java.lang.annotation.*;

/**
 * 用于表示DTO前缀，。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface DtoPrefix {
    String prefix() default "";
}
