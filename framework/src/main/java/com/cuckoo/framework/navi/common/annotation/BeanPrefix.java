package com.cuckoo.framework.navi.common.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface BeanPrefix {

    String value() default "";

}
