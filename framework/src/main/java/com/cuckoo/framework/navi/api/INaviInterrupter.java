package com.cuckoo.framework.navi.api;

import com.cuckoo.framework.navi.common.NaviBusinessException;

import java.util.List;

/**
 * 业务前后操作中断处理
 *
 */
public interface INaviInterrupter {

    boolean preAction(NaviHttpRequest request, NaviHttpResponse response, List<NaviParamter> parameters) throws NaviBusinessException;

    boolean postAction(NaviHttpRequest request, NaviHttpResponse response) throws NaviBusinessException;

}
