package com.java.navi.demo.action;

import com.java.navi.demo.dto.db.TDemo;
import com.java.navi.demo.dto.db.TDemoString;
import com.java.navi.demo.model.DemoModel;
import com.youku.java.navi.common.Rest;
import com.youku.java.navi.dto.BaseResult;
import com.youku.java.navi.server.api.ANaviAction;
import com.youku.java.navi.server.api.NaviHttpRequest;
import com.youku.java.navi.server.api.NaviHttpResponse;
import com.youku.java.navi.server.api.NaviJsonResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * demo action
 *
 * @author sgran<sunguangran@youku.com>
 * @since 16/5/23
 */
@Slf4j
@Rest("/test")
@Controller
public class DemoAction extends ANaviAction {

    @Autowired
    private DemoModel demoModel;

    @Rest
    public void create(NaviHttpRequest request, NaviHttpResponse response) throws Exception {
        BaseResult<TDemo> demoRes = demoModel.createDemoModel("ccid", "name");
        if (!demoRes.success()) {
            response.setResponseData(new NaviJsonResponseData(demoRes.code(), demoRes.msg()));
            return;
        }

        // 返回结果数据
        NaviJsonResponseData responseData = new NaviJsonResponseData(demoRes.getData().toJson());
        response.setResponseData(responseData);
    }

    @Rest("screate")
    public void createDemoString(NaviHttpRequest request, NaviHttpResponse response) throws Exception {
        BaseResult<TDemoString> demoRes = demoModel.createDemoStringModel("ccid", "name");
        if (!demoRes.success()) {
            response.setResponseData(new NaviJsonResponseData(demoRes.code(), demoRes.msg()));
            return;
        }

        // 返回结果数据
        NaviJsonResponseData responseData = new NaviJsonResponseData(demoRes.getData().toJson());
        response.setResponseData(responseData);
    }

}
