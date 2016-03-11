package com.youku.java.navi.server.serviceobj;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface TableInfo {
    String name() default "";
}
