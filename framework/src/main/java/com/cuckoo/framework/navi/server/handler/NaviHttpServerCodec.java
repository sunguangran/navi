package com.cuckoo.framework.navi.server.handler;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpServerCodec;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class NaviHttpServerCodec extends HttpServerCodec {

    public static final String BAD_REQUEST_URI = "/navi-bad-http-request";

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        try {
            super.handleUpstream(ctx, e);
        } catch (Exception ex) {
            Channel channel = ctx.getChannel();
            if (!channel.isOpen()) {
                return;
            }
            ctx.sendUpstream(new UpstreamMessageEvent(channel, new NaviBadRequest(ex), channel.getRemoteAddress()));
        }
    }


    public static class NaviBadRequest extends DefaultHttpRequest {

        private Throwable t;

        public NaviBadRequest(Throwable t) {
            super(HttpVersion.HTTP_1_1, HttpMethod.GET, BAD_REQUEST_URI);
            this.t = t;
        }

        public Throwable getT() {
            return t;
        }

        public void setT(Throwable t) {
            this.t = t;
        }

    }

}
