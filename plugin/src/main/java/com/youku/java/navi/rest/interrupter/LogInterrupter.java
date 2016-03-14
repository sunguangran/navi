package com.youku.java.navi.rest.interrupter;

import com.youku.java.navi.common.exception.NaviBusinessException;
import com.youku.java.navi.server.ServerConfigure;
import com.youku.java.navi.server.api.INaviInterrupter;
import com.youku.java.navi.server.api.NaviHttpRequest;
import com.youku.java.navi.server.api.NaviHttpResponse;
import com.youku.java.navi.server.api.NaviParamter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author sgran<sunguangran@youku.com>
 * @since 2015/6/2
 */
@Slf4j
@Setter
public class LogInterrupter implements INaviInterrupter {

    @Override
    public boolean preAction(NaviHttpRequest request, NaviHttpResponse response, List<NaviParamter> parameters) throws NaviBusinessException {
        if (!ServerConfigure.isDeployEnv()) {
            log.info("=> " + request.getUri() + " - " + request.getParams().toString());
        }
        request.getParams().put("calling_time_log", System.currentTimeMillis());

        return true;
    }

    @Override
    public boolean postAction(NaviHttpRequest request, NaviHttpResponse response) throws NaviBusinessException {
        if (!ServerConfigure.isDeployEnv()) {
            log.info("<= " + (response.getResponseData().toString()));
        }

        return true;
    }
}
