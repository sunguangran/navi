package com.cuckoo.framework.navi.test;

import com.cuckoo.framework.navi.api.ANaviAction;
import com.cuckoo.framework.navi.api.NaviHttpRequest;
import com.cuckoo.framework.navi.api.NaviHttpResponse;
import com.cuckoo.framework.navi.engine.datasource.service.NaviHttpClientService;
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
