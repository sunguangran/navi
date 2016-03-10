package com.youku.java.navi.server;

import com.youku.java.navi.boot.NaviDefine;
import com.youku.java.navi.server.handler.*;
import com.youku.java.navi.server.stats.NaviHttpServerStats;
import com.youku.java.navi.server.stats.NaviHttpServerStatsHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * 实现Http协议服务
 */
@Slf4j
@SuppressWarnings("unused")
public class NaviNettyServer extends ANaviTCPServer {

    private final static String MBEANNM = "com.cuckoo.framework.navi.stats:type=NaviHttpServerStats";

    private NaviHttpServerStatsHandler globalTcHandler;
    private Timer tcTimer;
    private NaviExecutionHandler executionHandler;

    public NaviNettyServer() {
        tcTimer = new HashedWheelTimer();
        NaviHttpServerStats httpServerStats = new NaviHttpServerStats();
        globalTcHandler = new NaviHttpServerStatsHandler(tcTimer, httpServerStats);

        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(httpServerStats, new ObjectName(MBEANNM));
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    @Override
    public ChannelPipelineFactory getPipelineFactory() {
        executionHandler = new NaviExecutionHandler();

        return new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("httpCodec", new NaviHttpServerCodec());
                pipeline.addLast("GLOBAL_TRAFFIC_SHAPING", globalTcHandler);

                String chunkSize = ServerConfigure.get(NaviDefine.CHUNK_AGGR_SIZE);
                if (StringUtils.isNumeric(chunkSize)) {
                    pipeline.addLast("aggregator", new HttpChunkAggregator(Integer.valueOf(chunkSize)));
                }

                // pipeline.addLast("encoder", new HttpResponseEncoder());
                pipeline.addLast("deflater", new HttpContentCompressor());
                pipeline.addLast("execution", executionHandler);
                pipeline.addLast("idleState", new IdleStateHandler(timer, getChildChannelIdleTime(), getChildChannelIdleTime(), getChildChannelIdleTime()));
                pipeline.addLast("handler", getNaviHttpHandler());
                return pipeline;
            }
        };
    }

    @Override
    protected NaviServerType getServerType() {
        return NaviServerType.NettyServer;
    }

    private NaviNettyHttpHandler getNaviHttpHandler() {
        NaviNettyHttpHandler handler = new NaviNettyHttpHandler(channelGroup);

        // business request listener
        handler.register(new FaviousRequestLister());
        handler.register(new NoNormalRequestLister());
        handler.register(new NaviRequestBusiListener());
        return handler;
    }

    @Override
    public void stopServer() {
        super.stopServer();

        if (globalTcHandler != null) {
            globalTcHandler.releaseExternalResources();
        }

        if (tcTimer != null) {
            tcTimer.stop();
        }

        if (executionHandler != null) {
            executionHandler.releaseExternalResources();
        }
    }
}
