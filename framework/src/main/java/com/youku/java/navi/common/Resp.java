package com.youku.java.navi.common;

import java.lang.annotation.*;

/**
 * dto类属性转换注解
 *
 * @author sgran<sgran@msn.cn>
 * @since 2016/03/07
 */
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Resp {

    String value() default "";

    boolean ip() default false;

    boolean encode() default false;

}
