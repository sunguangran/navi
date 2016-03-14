package com.youku.java.navi.server.api;

import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.exception.NaviBusinessException;

import java.util.List;

public class ParameterNullValidator implements INaviInterrupter {

    public boolean preAction(NaviHttpRequest request, NaviHttpResponse response, List<NaviParamter> parameters) throws NaviBusinessException {
        if (parameters == null) {
            return true;
        }
        for (NaviParamter param : parameters) {
            if (param.isRequiered() && request.isEmpty(param.getName())) {
                throw new NaviBusinessException(param.getName() + " is required", NaviError.ERR_PARAMS);
            }
        }

        return true;
    }

    public boolean postAction(NaviHttpRequest request, NaviHttpResponse response) throws NaviBusinessException {
        return true;
    }

}
