package com.java.navi.demo.actions;

import com.youku.java.navi.common.Rest;
import com.youku.java.navi.server.api.ANaviAction;
import com.youku.java.navi.server.api.NaviHttpRequest;
import com.youku.java.navi.server.api.NaviHttpResponse;
import com.youku.java.navi.server.api.NaviJsonResponseData;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Slf4j
@Rest("/test/aaa")
public class EchoAction extends ANaviAction {

    @Rest("/do.json")
    public void echo(NaviHttpRequest request, NaviHttpResponse response) throws Exception {
        response.setResponseData(new NaviJsonResponseData(0, request.getParameter("msg")));
    }

    @Rest("/do1.json")
    public void echo2(NaviHttpRequest request, NaviHttpResponse response) throws Exception {
        response.setResponseData(new NaviJsonResponseData(0, request.getParameter("count") + ".."));
    }

}
