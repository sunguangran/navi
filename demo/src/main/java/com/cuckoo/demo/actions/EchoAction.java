package com.cuckoo.demo.actions;

import com.cuckoo.framework.navi.common.annotation.Param;
import com.cuckoo.framework.navi.common.annotation.Rest;
import com.cuckoo.framework.navi.server.api.ANaviAction;
import com.cuckoo.framework.navi.server.api.NaviHttpResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Slf4j
@Rest("/demo/test/aaa")
public class EchoAction extends ANaviAction {

    @Rest("/do.json")
    public void echo(@Param("msg") String msg, NaviHttpResponse response) throws Exception {
        response.setResponseData(NaviSimpleJsonRespData.createInstance(0, msg));
    }

    @Rest("/do1.json")
    public void echo2(@Param("count") int count, NaviHttpResponse response) throws Exception {
        response.setResponseData(NaviSimpleJsonRespData.createInstance(0, count + ".."));
    }

}
