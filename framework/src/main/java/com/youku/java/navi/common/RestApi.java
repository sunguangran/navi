package com.youku.java.navi.common;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

/**
 * @author sgran<sunguangran@youku.com>
 * @since 2015/12/21
 */
@Setter
@Getter
public class RestApi {

    private String uri;
    private Class clazz;
    private Method method;

}
