package com.youku.java.navi.server.handler;

import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * 处理非正规请求
 */
@Slf4j
public class NoNormalRequestLister implements INaviHttpRequestListener {

    public boolean process(HttpRequest request, HttpResponse response) throws Exception {
        if (request instanceof NaviHttpServerCodec.NaviBadRequest) {
            // 无效HTTP协议请求
            response.setStatus(HttpResponseStatus.BAD_REQUEST);
            NaviHttpServerCodec.NaviBadRequest badRequest = (NaviHttpServerCodec.NaviBadRequest) request;
            log.error(badRequest.getT().getMessage(), badRequest.getT());

            return false;
        }

        return true;
    }

}
