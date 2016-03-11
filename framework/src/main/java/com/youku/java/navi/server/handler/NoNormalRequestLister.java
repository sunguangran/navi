package com.youku.java.navi.server.handler;

import com.youku.java.navi.server.ServerConfigure;
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
        String uri = request.getUri();
        if (request instanceof NaviHttpServerCodec.NaviBadRequest) {
            // 无效HTTP协议请求
            response.setStatus(HttpResponseStatus.BAD_REQUEST);
            NaviHttpServerCodec.NaviBadRequest badRequest = (NaviHttpServerCodec.NaviBadRequest) request;
            log.error(badRequest.getT().getMessage(), badRequest.getT());

            return false;
        } else if (uri != null) {
            String[] uriSplits = uri.split("/");
            if (uriSplits.length == 3 && ServerConfigure.isDeployEnv()) {
                // 处理历史不规范url请求
                StringBuilder builder = new StringBuilder();
                builder.append(uriSplits[0]);
                builder.append("/").append(uriSplits[1]);
                builder.append("/").append(uriSplits[1]);
                builder.append("/").append(uriSplits[2]);

                request.setUri(builder.toString());
            }

            return true;
        }

        return true;
    }

}
