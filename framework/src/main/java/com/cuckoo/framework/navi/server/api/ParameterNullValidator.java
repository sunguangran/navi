package com.cuckoo.framework.navi.server.api;

import com.cuckoo.framework.navi.common.NaviError;
import com.cuckoo.framework.navi.common.exception.NaviBusiException;

import java.util.List;

public class ParameterNullValidator implements INaviInterrupter {

    public boolean preAction(NaviHttpRequest request, NaviHttpResponse response, List<NaviParameter> parameters) throws NaviBusiException {
        if (parameters == null) {
            return true;
        }
        for (NaviParameter param : parameters) {
            if (param.isRequired() && request.isEmpty(param.getName())) {
                throw new NaviBusiException(param.getName() + " is required", NaviError.PARAM_ERROR.code());
            }
        }

        return true;
    }

    public boolean postAction(NaviHttpRequest request, NaviHttpResponse response) throws NaviBusiException {
        return true;
    }

}
