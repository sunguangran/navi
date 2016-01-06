package com.cuckoo.framework.navi.server;

import com.cuckoo.framework.navi.boot.NaviProps;
import com.cuckoo.framework.navi.server.handler.BadPacketRequestListener;
import com.cuckoo.framework.navi.server.handler.NaviExecutionHandler;
import com.cuckoo.framework.navi.server.handler.NaviPacketBusiListener;
import com.cuckoo.framework.navi.server.handler.NaviPacketHandler;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

public class NaviNettyTCPServer extends ANaviTCPServer {
    private Timer tcTimer;
    private NaviExecutionHandler executionHandler;
    private ChannelBuffer delimiter;
    private NaviPacketHandler handler;

    public NaviNettyTCPServer() {
        tcTimer = new HashedWheelTimer();
    }

    public ChannelPipelineFactory getPipelineFactory() {
        executionHandler = new NaviExecutionHandler();
//		execution = new ExecutionHandler(Executors.newCachedThreadPool());
        return new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("idleState", new IdleStateHandler(timer,
                    getChildChannelIdleTime(), getChildChannelIdleTime(),
                    getChildChannelIdleTime()));
                //StateCheckChannelHandler加入心跳机制  读空闲 断开连接 写空闲发送心跳数据
//				pipeline.addLast("idleHandler", new StateCheckChannelHandler());
                pipeline.addLast("decoder", new DelimiterBasedFrameDecoder(getMaxPacketSize(), getDelimiter()));
                pipeline.addLast("execution", executionHandler);
//				pipeline.addLast("execution", execution);
                pipeline.addLast("handler", getNaviTCPHandler());
                return pipeline;
            }
        };
    }

    public ChannelBuffer getDelimiter() {
        if (null != delimiter) {
            return delimiter;
        }
        String packetDelimiter = NaviProps.DEFAULT_PACKET_DELIMITER;
        byte[] delimiterBytes = packetDelimiter.getBytes();
        if (ServerConfigure.containsKey(NaviProps.PACKET_DELIMITER)) {
            packetDelimiter = ServerConfigure.get(NaviProps.PACKET_DELIMITER);
            delimiterBytes = packetDelimiter.getBytes();
            if (null != packetDelimiter && packetDelimiter.contains("\\")) {
                delimiterBytes = new byte[]{Byte.parseByte(packetDelimiter.replace("\\", ""))};
            }
        }
        delimiter = ChannelBuffers.wrappedBuffer(delimiterBytes);
        return delimiter;
    }

    public int getMaxPacketSize() {
        int maxSize = NaviProps.DEFAULT_MAX_TCP_PACKET_SIZE;
        if (ServerConfigure.containsKey(NaviProps.TCP_MAX_PACKET_SIZE)) {
            maxSize = Integer.parseInt(ServerConfigure.get(NaviProps.TCP_MAX_PACKET_SIZE));
        }
        return maxSize;
    }

    private NaviPacketHandler getNaviTCPHandler() {
        if (null != handler) {
            return handler;
        }
        handler = new NaviPacketHandler();
        // busi req listener
        handler.register(new BadPacketRequestListener());
        handler.register(new NaviPacketBusiListener());
        return handler;
    }

    @Override
    public void stopServer() {
        super.stopServer();
        if (tcTimer != null) {
            tcTimer.stop();
        }
        if (executionHandler != null) {
            executionHandler.releaseExternalResources();
        }
    }
}
