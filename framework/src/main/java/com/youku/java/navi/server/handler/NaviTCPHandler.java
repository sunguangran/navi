package com.youku.java.navi.server.handler;

import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;

/**
 * 仅仅处理通信层TCP协议请求
 */
@Slf4j
public class NaviTCPHandler extends SimpleChannelUpstreamHandler {

    private ChannelGroup channelGroup;

    public NaviTCPHandler(ChannelGroup channelGroup) {
//		super(timer, channelIdleSecond, channelIdleSecond, channelIdleSecond);
        this.channelGroup = channelGroup;
        // session计数
        SessionIdCounter.getInstance().incAndGet();
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        channelGroup.add(e.getChannel());
        log.debug("add current channel to group!");
        super.channelOpen(ctx, e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        Throwable cause = e.getCause();
        log.error("Exception caught on session id " + SessionIdCounter.getInstance() + "," + cause.toString(), cause);
        if (e.getFuture().getChannel().isOpen()) {
            e.getFuture().addListener(ChannelFutureListener.CLOSE);
        }
    }
}
