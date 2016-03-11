package com.youku.java.navi.server.handler;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 * 请求实际处理前后的监听器
 */
public interface INaviHttpRequestListener {

    /**
     * 处理请求
     */
    boolean process(HttpRequest request, HttpResponse response) throws Exception;

}
