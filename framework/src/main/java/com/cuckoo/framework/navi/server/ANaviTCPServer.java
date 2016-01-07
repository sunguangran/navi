package com.cuckoo.framework.navi.server;

import com.cuckoo.framework.navi.boot.NaviDefine;
import com.cuckoo.framework.navi.server.module.NaviModuleContextFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import java.net.InetSocketAddress;
import java.rmi.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 实现TCP/IP服务
 */
@Slf4j
public abstract class ANaviTCPServer extends ANaviServer {

    protected DefaultChannelGroup channelGroup = new DefaultChannelGroup("navi-channels");
    private ServerBootstrap bootstrap;

    protected Timer timer = new HashedWheelTimer();

    @Override
    protected boolean preStartServer(Properties serverCfg) {
        ServerConfigure.setServerCfg(serverCfg);
        log.info("prepare for starting server, current work mode is " + ServerConfigure.getWorkMode() + ".");
        return true;
    }

    @Override
    protected int startServer() {
        log.info("start listening the port " + ServerConfigure.getPort() + ".");

        try {
            ExecutorService executor = Executors.newCachedThreadPool();
            NioServerSocketChannelFactory channelFactory = new NioServerSocketChannelFactory(executor, executor);
            ChannelPipelineFactory pipelineFactory = getPipelineFactory();

            bootstrap = new ServerBootstrap(channelFactory);
            bootstrap.setPipelineFactory(pipelineFactory);

            if (ServerConfigure.containsKey(NaviDefine.BACKLOG)) {
                bootstrap.setOption(NaviDefine.BACKLOG, ServerConfigure.get(NaviDefine.BACKLOG));
            }

            if (ServerConfigure.containsKey(NaviDefine.REUSEADDRESS)) {
                bootstrap.setOption(NaviDefine.REUSEADDRESS, Boolean.valueOf(ServerConfigure.get(NaviDefine.REUSEADDRESS)));
            }

            if (ServerConfigure.containsKey(NaviDefine.CHILD_KEEPALIVE)) {
                bootstrap.setOption(NaviDefine.CHILD_KEEPALIVE, Boolean.valueOf(ServerConfigure.get(NaviDefine.CHILD_KEEPALIVE)));
            }

            if (ServerConfigure.containsKey(NaviDefine.CHILD_TCPNODELAY)) {
                bootstrap.setOption(NaviDefine.CHILD_TCPNODELAY, Boolean.valueOf(ServerConfigure.get(NaviDefine.CHILD_TCPNODELAY)));
            }
            if (ServerConfigure.containsKey(ServerConfigure.CHILD_SENDBUFFERSIZE)) {
                bootstrap.setOption(ServerConfigure.CHILD_SENDBUFFERSIZE, Integer.valueOf(ServerConfigure.get(ServerConfigure.CHILD_SENDBUFFERSIZE)));
            }
            if (ServerConfigure.containsKey(ServerConfigure.CHILD_RECEIVEBUFFERSIZE)) {
                bootstrap.setOption(ServerConfigure.CHILD_RECEIVEBUFFERSIZE, Integer.valueOf(ServerConfigure.get(ServerConfigure.CHILD_RECEIVEBUFFERSIZE)));
            }
            if (ServerConfigure.containsKey(ServerConfigure.WRITEBUFFERHIGHWATERMARK)) {
                bootstrap.setOption(ServerConfigure.WRITEBUFFERHIGHWATERMARK, Integer.valueOf(ServerConfigure.get(ServerConfigure.WRITEBUFFERHIGHWATERMARK)));
            }
            if (ServerConfigure.containsKey(ServerConfigure.WRITEBUFFERLOWWARTERMARK)) {
                bootstrap.setOption(ServerConfigure.WRITEBUFFERLOWWARTERMARK, Integer.valueOf(ServerConfigure.get(ServerConfigure.WRITEBUFFERLOWWARTERMARK)));
            }

            String port = ServerConfigure.getPort();
            if (!StringUtils.isEmpty(port)) {
                channelGroup.add(
                    bootstrap.bind(new InetSocketAddress("0.0.0.0", Integer.parseInt(port)))
                );
            } else {
                throw new UnknownHostException("the server port has not been setted.");
            }
        } catch (RuntimeException e) {
            log.error("server starting failed, " + e.getMessage());
            return FAILED;
        } catch (Exception e) {
            log.error("server starting failed, " + e.getMessage());
            return FAILED;
        }

        return SUCCESS;
    }

    public abstract ChannelPipelineFactory getPipelineFactory();

    @Override
    protected void postStartServer() {
        // 初始化ModuleFactory，检测模块版本
        NaviModuleContextFactory.getInstance().startCheckModuleProccess();

        log.info("the providing server is " + ServerConfigure.getServer() + ", the listening port is " + ServerConfigure.getPort() + ".");
        log.info("navi server has been started successfully.");
    }

    @Override
    protected NaviServerType getServerType() {
        return NaviServerType.NettyServer;
    }

    @Override
    public void stopServer() {
        if (channelGroup != null) {
            ChannelGroupFuture future = channelGroup.close();
            future.awaitUninterruptibly();
        }

        if (bootstrap != null) {
            bootstrap.getFactory().releaseExternalResources();
        }

        if (timer != null) {
            timer.stop();
        }
    }

    protected int getChildChannelIdleTime() {
        try {
            return Integer.valueOf(ServerConfigure.get(NaviDefine.CHILD_CHANNEL_IDLTIME));
        } catch (Exception e) {
            return 0;
        }
    }
}
