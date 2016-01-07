package com.cuckoo.framework.navi.common;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

/**
 * @author sgran<sgran@msn.cn>
 * @since 2015/12/21
 */
@Setter
@Getter
public class RestApi {

    private String uri;
    private String module;
    private Class clazz;
    private Method method;

}
