package com.cuckoo.framework.navi.server.handler;

import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;

import java.net.SocketTimeoutException;

@Slf4j
public class StateCheckChannelHandler extends IdleStateAwareChannelHandler {

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
        if (e.getState() == IdleState.WRITER_IDLE) {
            Channel channel = e.getChannel();
            if (channel != null) {
                //写空闲  则发送心跳数据
                channel.write(Heartbeat.getSingleton());
            } else {
                log.warn("writer idle on channel({}), but hsfChannel is not managed.", e.getChannel());
            }
        } else if (e.getState() == IdleState.READER_IDLE) {
            //读空闲 则断开连接
            log.error("channel:{} is time out.", e.getChannel());
            handleUpstream(ctx, new DefaultExceptionEvent(e.getChannel(), new SocketTimeoutException("force to close channel(" + ctx.getChannel().getRemoteAddress() + "), reason: time out.")));
            e.getChannel().close();
        }
        super.channelIdle(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() != Heartbeat.getSingleton()) {
            super.messageReceived(ctx, e);
        }
    }
}
