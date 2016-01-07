package com.cuckoo.framework.navi.server.handler;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * 处理HTTP协议的Handler
 */
public abstract class AbstractNaviNettyHttpHandler extends NaviTCPHandler {

    public AbstractNaviNettyHttpHandler(ChannelGroup channelGroup) {
        super(channelGroup);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        HttpRequest request = (HttpRequest) e.getMessage();
        decorateAttachment(ctx);
        handle(request, e.getChannel());
        ctx.sendUpstream(e);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        returnAttachment(ctx);
        super.channelDisconnected(ctx, e);
    }

    public boolean isReceived(ChannelHandlerContext ctx) {
        return ctx.getAttachment() != null && ctx.getAttachment() instanceof NaviAttachment;
    }

    public void decorateAttachment(ChannelHandlerContext ctx) {
        ctx.setAttachment(new NaviAttachment(ctx.getAttachment()));
    }

    public void returnAttachment(ChannelHandlerContext ctx) {
        if (isReceived(ctx)) {
            NaviAttachment att = (NaviAttachment) ctx.getAttachment();
            ctx.setAttachment(att.obj);
        }
    }

    public abstract void handle(HttpRequest request, Channel channel) throws Exception;

    private class NaviAttachment {
        Object obj;

        NaviAttachment(Object obj) {
            this.obj = obj;
        }
    }

}
