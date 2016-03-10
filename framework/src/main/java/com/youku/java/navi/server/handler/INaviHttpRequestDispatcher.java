package com.youku.java.navi.server.handler;


import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

public interface INaviHttpRequestDispatcher {

    void process(HttpRequest request, HttpResponse response) throws Exception;

}
