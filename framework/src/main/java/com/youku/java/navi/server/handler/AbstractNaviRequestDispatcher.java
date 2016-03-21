package com.youku.java.navi.server.handler;

import com.youku.java.navi.common.exception.NaviBusinessException;
import com.youku.java.navi.server.api.INaviResponseData;
import com.youku.java.navi.server.api.NaviHttpRequest;
import com.youku.java.navi.server.api.NaviHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import java.lang.reflect.InvocationTargetException;

@Slf4j
public abstract class AbstractNaviRequestDispatcher implements INaviHttpRequestDispatcher {

    /**
     * 根据原生的请求信息封装NaviHttpRequest以便使用
     */
    public abstract NaviHttpRequest packageNaviHttpRequest(HttpRequest request) throws Exception;

    /**
     * 通过解析的信息直接加载API信息执行
     */
    public abstract void callApi(NaviHttpRequest request, NaviHttpResponse response) throws Exception;

    public void process(HttpRequest request, HttpResponse response) throws Exception {
        long t = System.currentTimeMillis();
        try {
            NaviHttpRequest naviRequest = packageNaviHttpRequest(request);
            if (naviRequest != null) {
                NaviHttpResponse naviResponse = (NaviHttpResponse) response;
                callApi(naviRequest, naviResponse);
                t = System.currentTimeMillis() - t;
                INaviResponseData responseData = naviResponse.getResponseData();
                responseData.setCost(t);
                naviResponse.setContent(responseData.getResponseData());
                naviResponse.headers().set(Names.CONTENT_TYPE, responseData.getResponseType());
            } else {
                log.warn("Navi Request is null!");
            }
        } catch (Exception e) {
            // 反射异常
            if (e instanceof InvocationTargetException) {
                // bussiness异常会经过反射异常封装抛出
                if (((InvocationTargetException) e).getTargetException() instanceof NaviBusinessException) {
                    t = System.currentTimeMillis() - t;
                    NaviBusinessException ee = (NaviBusinessException) ((InvocationTargetException) e).getTargetException();
                    ee.setCost(t);
                    response.setContent(ee.getResponseData());
                    response.headers().set(Names.CONTENT_TYPE, ee.getResponseType());
                    return;
                }
            }

            if (e instanceof NaviBusinessException) {
                t = System.currentTimeMillis() - t;
                NaviBusinessException ee = (NaviBusinessException) e;
                ee.setCost(t);
                response.setContent(ee.getResponseData());
                response.headers().set(Names.CONTENT_TYPE, ee.getResponseType());
                return;
            }

            throw e;
        }
    }
}
