package com.youku.java.navi.rest.interrupter;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.youku.java.navi.common.exception.NaviBusinessException;
import com.youku.java.navi.server.api.INaviInterrupter;
import com.youku.java.navi.server.api.NaviHttpRequest;
import com.youku.java.navi.server.api.NaviHttpResponse;
import com.youku.java.navi.server.api.NaviParamter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;

/**
 * @author sgran<sunguangran@youku.com>
 * @since 2015/10/30
 */
@Slf4j
@Setter
public class XssInterrupter implements INaviInterrupter {

    @Override
    public boolean preAction(NaviHttpRequest request, NaviHttpResponse response, List<NaviParamter> parameters) throws NaviBusinessException {
        JSONObject params = request.getParams();

        Iterator ite = params.keySet().iterator();
        while (ite.hasNext()) {
            try {
                String key = String.valueOf(ite.next());
                String value = this.cleanXSS(params.getString(key));
                params.put(key, value);
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            }
        }

        return true;
    }

    @Override
    public boolean postAction(NaviHttpRequest request, NaviHttpResponse response) throws NaviBusinessException {
        return true;
    }

    private String cleanXSS(String value) {
        //You'll need to remove the spaces from the html entities below
        value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        value = value.replaceAll("\\(", "& #40;").replaceAll("\\)", "& #41;");
        value = value.replaceAll("'", "& #39;");
        value = value.replaceAll("eval\\((.*)\\)", "");
        value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
        value = value.replaceAll("script", "");
        return value;
    }

}
