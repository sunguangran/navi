package com.youku.java.navi.test;

import com.youku.java.navi.server.api.ANaviAction;
import com.youku.java.navi.server.api.NaviHttpRequest;
import com.youku.java.navi.server.api.NaviHttpResponse;
import com.youku.java.navi.engine.datasource.service.NaviHttpClientService;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestAPI extends ANaviAction {

    private NaviHttpClientService httpService;

    public void doAction(NaviHttpRequest request, NaviHttpResponse response)
        throws Exception {
        List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
        list.add(new BasicNameValuePair("ids", String.valueOf(123)));
        response.setJsonData(getHttpService().doPost("/passport/ykalipay/get_alipay_user", list));
    }

    public NaviHttpClientService getHttpService() {
        return httpService;
    }

    public void setHttpService(NaviHttpClientService httpService) {
        this.httpService = httpService;
    }

    public static void main(String[] args) {
        System.out.println(new Date(1326161770727L).toLocaleString());
    }

}
