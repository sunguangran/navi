package com.cuckoo.framework.navi.server.handler;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;


public class NaviRequestBusiListener implements INaviHttpRequestListener {

    private INaviHttpRequestDispatcher naviDispatcher;

    public NaviRequestBusiListener() {
        this.naviDispatcher = new DefaultNaviRequestDispatcher();
    }

    public boolean process(HttpRequest request, HttpResponse response) throws Exception {
        naviDispatcher.process(request, response);
        return true;
    }

}
