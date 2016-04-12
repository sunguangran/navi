package com.youku.java.navi.server.handler;

import com.youku.java.navi.server.ServerConfigure;
import com.youku.java.navi.server.api.NaviHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class NaviNettyHttpHandler extends AbstractNaviNettyHttpHandler {

    private List<INaviHttpRequestListener> listeners;
    private final static String WRITING = "writing";

    public NaviNettyHttpHandler(ChannelGroup channelGroup) {
        super(channelGroup);
        listeners = new ArrayList<>();
    }

    @Override
    public void handle(HttpRequest request, Channel channel) throws Exception {
        // 设置调用方ip，X-Forwarded-For 是使用了代理（如nginx）会附加在HTTP头域上的
        if (request.headers().contains("X-Forwarded-For")) {
            request.headers().set(Names.HOST, request.headers().get("X-Forwarded-For"));
        } else {
            InetSocketAddress insocket = (InetSocketAddress) channel.getRemoteAddress();
            request.headers().set(Names.HOST, insocket.getAddress().getHostAddress());
        }

        HttpResponse response = new NaviHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);

        for (INaviHttpRequestListener listener : listeners) {
            boolean res = listener.process(request, response);
            log.debug(listener.getClass().getName() + " is completed!");
            if (!res) {
                break;
            }
        }

        sendHttpResponse(channel, request, response);
    }

    /**
     * 注册请求处理监听
     */
    public void register(INaviHttpRequestListener listener) {
        listeners.add(listener);
    }

    private void sendHttpResponse(Channel channel, HttpRequest request, HttpResponse response) {
        if (!channel.isOpen()) {
            return;
        }

        // response的内容已在各Listener中填充
        response.headers().set(Names.CONTENT_LENGTH, response.getContent().readableBytes());
        response.headers().set(Names.SERVER, "NAVI/1.1.4(UNIX)");

        if (!HttpHeaders.isKeepAlive(request) || response.getStatus() != HttpResponseStatus.OK || ServerConfigure.isChannelClose()) {
            response.headers().set(Names.CONNECTION, "close");
            channel.setAttachment(WRITING);
            ChannelFuture f = channel.write(response);
            f.addListener(ChannelFutureListener.CLOSE);
            f.addListener(new ChannelFutureListener() {

                public void operationComplete(ChannelFuture f) throws Exception {
                    if (!f.isSuccess()) {
                        log.error(f.getCause().getMessage(), f.getCause());
                    }
                }
            });
        } else {
            if (request.getProtocolVersion() == HttpVersion.HTTP_1_0) {
                response.headers().add(Names.CONNECTION, "Keep-Alive");
            }
            channel.setAttachment(WRITING);
            ChannelFuture f = channel.write(response);
            f.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            f.addListener(new ChannelFutureListener() {

                public void operationComplete(ChannelFuture f) throws Exception {
                    if (!f.isSuccess()) {
                        log.error(f.getCause().getMessage(), f.getCause());
                    }
                }
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        Channel channel = ctx.getChannel();
        if (channel.getAttachment() != null && (Error.class.isAssignableFrom(channel.getAttachment().getClass()) || WRITING.equals(channel.getAttachment().toString()))) {
            return;
        }

        if (!isReceived(ctx)) {
            return;
        }

        try {
            log.error(e.getCause().getMessage(), e.getCause());
            channel.setAttachment(new Error());
            DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            response.setContent(ChannelBuffers.copiedBuffer(response.getStatus().toString() + ":" + e.getCause().getMessage(), CharsetUtil.UTF_8));
            response.headers().set(Names.CONTENT_LENGTH, response.getContent().readableBytes());
            response.headers().set(Names.SERVER, "NAVI/1.1.4(UNIX)");
            response.headers().set(Names.CONNECTION, "close");
            ChannelFuture f = channel.write(response);
            f.addListener(ChannelFutureListener.CLOSE);
            f.addListener(new ChannelFutureListener() {

                public void operationComplete(ChannelFuture f) throws Exception {
                    if (!f.isSuccess()) {
                        log.error(f.getCause().getMessage(), f.getCause());
                    }
                }
            });
        } catch (Exception ex) {
            log.error(e.getCause().getMessage(), e.getCause());
            e.getFuture().addListener(ChannelFutureListener.CLOSE);
        }
    }
}
