package com.cuckoo.framework.navi.serviceobj;

import java.lang.annotation.*;

/**
 * 用于标注HBase数据表信息的注解，有cf和column两个属性：cf为列族名，column为列名。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface DtoInfo {

    String cf() default "";

    String column() default "";

}
