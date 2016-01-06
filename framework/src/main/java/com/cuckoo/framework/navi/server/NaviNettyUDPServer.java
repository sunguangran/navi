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

public class NaviNettyUDPServer extends ANaviPacketServer {
    private NaviExecutionHandler executionHandler;

    public ChannelPipelineFactory getPipelineFactory() {
        executionHandler = new NaviExecutionHandler();
        return new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", new DelimiterBasedFrameDecoder(getMaxPacketSize(), getDelimiter()));
                pipeline.addLast("execution", executionHandler);
                pipeline.addLast("handler", getNaviUDPHandler());
                return pipeline;
            }
        };
    }

    public ChannelBuffer getDelimiter() {
        String packetDelimiter = NaviProps.DEFAULT_PACKET_DELIMITER;
        byte[] delimiterBytes = null;
        if (ServerConfigure.containsKey(NaviProps.PACKET_DELIMITER)) {
            packetDelimiter = ServerConfigure.get(NaviProps.PACKET_DELIMITER);
        }
        if (null != packetDelimiter && packetDelimiter.contains("\\")) {
            delimiterBytes = new byte[]{Byte.parseByte(packetDelimiter.replace("\\", ""))};
        } else {
            delimiterBytes = packetDelimiter.getBytes();
        }
        ChannelBuffer delimiter = ChannelBuffers.wrappedBuffer(delimiterBytes);
        return delimiter;
    }

    public int getMaxPacketSize() {
        int maxSize = NaviProps.DEFAULT_MAX_UDP_PACKET_SIZE;
        if (ServerConfigure.containsKey(NaviProps.UDP_MAX_PACKET_SIZE)) {
            maxSize = Integer.parseInt(ServerConfigure.get(NaviProps.UDP_MAX_PACKET_SIZE));
        }
        return maxSize;
    }

    private NaviPacketHandler getNaviUDPHandler() {
        NaviPacketHandler handler = new NaviPacketHandler();
        // busi req listener
        handler.register(new BadPacketRequestListener());
        handler.register(new NaviPacketBusiListener());
        return handler;
    }

    @Override
    public void stopServer() {
        super.stopServer();
        if (executionHandler != null) {
            executionHandler.releaseExternalResources();
        }
    }

}
