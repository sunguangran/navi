package com.youku.java.navi.server.handler;

import com.youku.java.navi.server.ServerConfigure;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.execution.ChannelUpstreamEventRunnable;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * netty ChannelPipeline的handler,用于异步处理messageEvent
 */
public class NaviExecutionHandler implements ChannelUpstreamHandler, NaviExecutionHandlerMBean {

    private static final String TOO_BUSY_URI = "too_busy_server";
    private ThreadPoolExecutor executeThreadPool;
    private int maxQueueBlockSize = 1000;
    private int minThreadPoolSize = Runtime.getRuntime().availableProcessors() * 2;
    private int maxThreadPoolSize = Runtime.getRuntime().availableProcessors() * 25;

    private final static String MBEANNM = "com.youku.java.navi.stats:type=NaviExecutionHandler";

    public NaviExecutionHandler() {

        maxQueueBlockSize = ServerConfigure.get("maxQueueBlockSize") == null ? maxQueueBlockSize : Integer.parseInt(ServerConfigure.get("maxQueueBlockSize"));
        minThreadPoolSize = ServerConfigure.get("minThreadPoolSize") == null ? minThreadPoolSize : Integer.parseInt(ServerConfigure.get("minThreadPoolSize"));
        maxThreadPoolSize = ServerConfigure.get("maxThreadPoolSize") == null ? maxThreadPoolSize : Integer.parseInt(ServerConfigure.get("maxThreadPoolSize"));

        executeThreadPool = new ThreadPoolExecutor(
            minThreadPoolSize, maxThreadPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(maxQueueBlockSize)
        );

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            mbs.registerMBean(this, new ObjectName(MBEANNM));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof MessageEvent) {
            try {
                executeThreadPool.execute(new ChannelUpstreamEventRunnable(ctx, e, executeThreadPool));
            } catch (RejectedExecutionException ex) {
                ctx.sendUpstream(
                    new UpstreamMessageEvent(ctx.getChannel(), new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, TOO_BUSY_URI), null)
                );
            }
        } else {
            ctx.sendUpstream(e);
        }
    }

    public void releaseExternalResources() {
        executeThreadPool.shutdown();
    }

    public int getQueueSize() {
        return executeThreadPool.getQueue().size();
    }

    public int getActiveCount() {
        return executeThreadPool.getActiveCount();
    }

}
