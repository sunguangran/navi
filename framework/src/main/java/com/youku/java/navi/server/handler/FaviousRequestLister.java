package com.youku.java.navi.server.handler;

import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;

/**
 * 屏蔽浏览器网站logo请求
 */
@Slf4j
public class FaviousRequestLister implements INaviHttpRequestListener {

    private static final String FAVICON = "/favicon.ico";

    public boolean process(HttpRequest request, HttpResponse response)
        throws Exception {
        String uri = request.getUri();
        if (uri.equalsIgnoreCase(FAVICON)) {
            response.setStatus(HttpResponseStatus.NOT_FOUND);
            log.warn("favicon.ico isn't found!");
            response.setContent(ChannelBuffers.copiedBuffer(HttpResponseStatus.NOT_FOUND.toString(), CharsetUtil.UTF_8));
            return false;
        }

        return true;
    }
}
