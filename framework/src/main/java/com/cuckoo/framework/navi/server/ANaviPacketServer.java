package com.cuckoo.framework.navi.server;

import com.cuckoo.framework.navi.boot.NaviProps;
import com.cuckoo.framework.navi.module.NaviModuleContextFactory;
import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import java.net.InetSocketAddress;
import java.rmi.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public abstract class ANaviPacketServer extends ANaviServer {
    private ConnectionlessBootstrap bootstrap;
    protected DefaultChannelGroup channelGroup = new DefaultChannelGroup(
        "navi-channels");
    protected Timer timer = new HashedWheelTimer();

    public void stopServer() {
        if (bootstrap != null) {
            bootstrap.getFactory().releaseExternalResources();
        }
    }

    @Override
    protected boolean preStartServer(Properties serverCfg) {
        // 设置为server共享信息
        ServerConfigure.setServerCfg(serverCfg);
        log.info("prepared for starting server successfully!");
        log.info("the server work mode is " + ServerConfigure.getWorkMode()
            + ".");
        return true;
    }

    @Override
    protected int startServer() {
        log.info("starting listening the port!");
        ExecutorService executor = Executors.newCachedThreadPool();
        DatagramChannelFactory channelFactory = new NioDatagramChannelFactory(executor);
        ChannelPipelineFactory pipelineFactory = getPipelineFactory();
        try {
            bootstrap = new ConnectionlessBootstrap(channelFactory);
            bootstrap.setPipelineFactory(pipelineFactory);
            configBootstrap(bootstrap);
            if (ServerConfigure.getPort() != null
                && ServerConfigure.getPort().length() != 0) {
                bootstrap.bind(new InetSocketAddress(Integer.parseInt(ServerConfigure.getPort())));
            } else {
                throw new UnknownHostException("the server port isn't setted");
            }
        } catch (RuntimeException e) {
            log.error("the server starting is fail!" + e.getMessage());
            return FAIL;
        } catch (Exception e) {
            log.error("the server starting is fail!" + e.getMessage());
            return FAIL;
        }

        return SUCCESS;
    }

    public abstract ChannelPipelineFactory getPipelineFactory();

    @Override
    protected void postStartServer() {
        // 初始化ModuleFactory，检测模块版本
        NaviModuleContextFactory.getInstance().startCheckModuleProccess();
        log.info("the providing server is " + ServerConfigure.getServer()
            + ",the listening port is " + ServerConfigure.getPort() + ".");
        log.info("the server has been started successfully!");
    }

    private void configBootstrap(ConnectionlessBootstrap bootstrap) {
        if (ServerConfigure.containsKey(NaviProps.BACKLOG)) {
            bootstrap.setOption(NaviProps.BACKLOG,
                ServerConfigure.get(NaviProps.BACKLOG));
        }

        if (ServerConfigure.containsKey(NaviProps.REUSEADDRESS)) {
            bootstrap.setOption(NaviProps.REUSEADDRESS, Boolean
                .valueOf(ServerConfigure
                    .get(NaviProps.REUSEADDRESS)));
        }

        if (ServerConfigure.containsKey(NaviProps.CHILD_KEEPALIVE)) {
            bootstrap
                .setOption(
                    NaviProps.CHILD_KEEPALIVE,
                    Boolean.valueOf(ServerConfigure
                        .get(NaviProps.CHILD_KEEPALIVE)));
        }

        if (ServerConfigure.containsKey(NaviProps.CHILD_TCPNODELAY)) {
            bootstrap
                .setOption(
                    NaviProps.CHILD_TCPNODELAY,
                    Boolean.valueOf(ServerConfigure
                        .get(NaviProps.CHILD_TCPNODELAY)));
        }
        if (ServerConfigure.containsKey(ServerConfigure.CHILD_SENDBUFFERSIZE)) {
            bootstrap
                .setOption(
                    ServerConfigure.CHILD_SENDBUFFERSIZE,
                    Integer.valueOf(ServerConfigure
                        .get(ServerConfigure.CHILD_SENDBUFFERSIZE)));
        }
        if (ServerConfigure
            .containsKey(ServerConfigure.CHILD_RECEIVEBUFFERSIZE)) {
            bootstrap
                .setOption(
                    ServerConfigure.CHILD_RECEIVEBUFFERSIZE,
                    Integer.valueOf(ServerConfigure
                        .get(ServerConfigure.CHILD_RECEIVEBUFFERSIZE)));
        }
        if (ServerConfigure
            .containsKey(ServerConfigure.WRITEBUFFERHIGHWATERMARK)) {
            bootstrap
                .setOption(
                    ServerConfigure.WRITEBUFFERHIGHWATERMARK,
                    Integer.valueOf(ServerConfigure
                        .get(ServerConfigure.WRITEBUFFERHIGHWATERMARK)));
        }
        if (ServerConfigure
            .containsKey(ServerConfigure.WRITEBUFFERLOWWARTERMARK)) {
            bootstrap
                .setOption(
                    ServerConfigure.WRITEBUFFERLOWWARTERMARK,
                    Integer.valueOf(ServerConfigure
                        .get(ServerConfigure.WRITEBUFFERLOWWARTERMARK)));
        }
    }

    @Override
    protected NaviServerType getServerType() {
        return NaviServerType.NettyServer;
    }


    protected int getChildChannelIdleTime() {
        try {
            return Integer.valueOf(ServerConfigure
                .get(NaviProps.CHILD_CHANNEL_IDLTIME));
        } catch (Exception e) {
            return 0;
        }
    }
}
