package com.cuckoo.framework.navi.server.handler;

import com.cuckoo.framework.navi.common.exception.NaviBusinessException;
import com.cuckoo.framework.navi.server.api.INaviResponseData;
import com.cuckoo.framework.navi.server.api.NaviHttpRequest;
import com.cuckoo.framework.navi.server.api.NaviHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

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
        long timestamp = System.currentTimeMillis();
        try {
            NaviHttpRequest req = packageNaviHttpRequest(request);
            if (req != null) {
                NaviHttpResponse resp = (NaviHttpResponse) response;
                callApi(req, resp);
                INaviResponseData data = resp.getResponseData();
                data.setCost(System.currentTimeMillis() - timestamp);
                resp.setContent(data.getResponseData());
                resp.headers().set(Names.CONTENT_TYPE, data.getResponseType());
            } else {
                log.warn("navi request is null.");
            }
        } catch (Exception e) {
            if (e instanceof NaviBusinessException) {
                timestamp = System.currentTimeMillis() - timestamp;
                NaviBusinessException alias = (NaviBusinessException) e;
                alias.setCost(timestamp);

                response.setContent(alias.getResponseData());
                response.headers().set(Names.CONTENT_TYPE, alias.getResponseType());
                return;
            }

            throw e;
        }
    }
}
