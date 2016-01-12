package com.cuckoo.framework.navi.server.handler;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;


public class NaviRequestBusiListener implements INaviHttpRequestListener {

    private INaviHttpRequestDispatcher dispatcher;

    public NaviRequestBusiListener() {
        this.dispatcher = new DefaultNaviRequestDispatcher();
    }

    public boolean process(HttpRequest request, HttpResponse response) throws Exception {
        dispatcher.process(request, response);
        return true;
    }

}
