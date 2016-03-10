package com.youku.java.navi.server.stats;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.traffic.GlobalTrafficShapingHandler;
import org.jboss.netty.util.Timer;

public class NaviHttpServerStatsHandler extends GlobalTrafficShapingHandler {

    final private static int DEFAULT_CHECK_INTERVAL = 5 * 60 * 1000; // 5min
    private NaviHttpServerStatsMBean statsMBean;

    public NaviHttpServerStatsHandler(Timer timer, NaviHttpServerStatsMBean statsMBean) {
        super(timer, DEFAULT_CHECK_INTERVAL);
        this.statsMBean = statsMBean;
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelOpen(ctx, e);
        statsMBean.incrConnCount();
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelClosed(ctx, e);
        statsMBean.decrConnCount();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent evt) throws Exception {
        try {
            super.messageReceived(ctx, evt);
        } finally {
            statsMBean.setReadByte(getTrafficCounter().getCumulativeReadBytes());
            statsMBean.incrReadCount();
        }
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent evt) throws Exception {
        try {
            super.writeRequested(ctx, evt);
        } finally {
            statsMBean.setWriteByte(getTrafficCounter().getCumulativeWrittenBytes());
            statsMBean.incrWriteCount();
        }
    }
}
