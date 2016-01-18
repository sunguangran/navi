package com.cuckoo.framework.navi.server.api;

import com.cuckoo.framework.navi.common.exception.NaviBusiException;

import java.util.List;

/**
 * 业务前后操作中断处理
 */
public interface INaviInterrupter {

    boolean preAction(NaviHttpRequest request, NaviHttpResponse response, List<NaviParameter> parameters) throws NaviBusiException;

    boolean postAction(NaviHttpRequest request, NaviHttpResponse response) throws NaviBusiException;

}
