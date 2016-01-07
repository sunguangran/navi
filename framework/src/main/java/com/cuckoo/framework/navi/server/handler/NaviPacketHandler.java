package com.cuckoo.framework.navi.server.handler;

import com.cuckoo.framework.navi.api.NaviRequestPacket;
import com.cuckoo.framework.navi.api.NaviResponsePacket;
import com.cuckoo.framework.navi.boot.NaviDefine;
import com.cuckoo.framework.navi.server.ServerConfigure;
import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class NaviPacketHandler extends AbstractNaviPacketHandler {
    private List<INaviPacketListener> listeners;
    protected String protocol;

    public NaviPacketHandler() {
        listeners = new ArrayList<INaviPacketListener>();
    }

    @Override
    public void handle(NaviRequestPacket udpRequest, Channel channel)
        throws Exception {
        NaviResponsePacket response = new NaviResponsePacket();
        response.setRemoteAddress(udpRequest.getRemoteAddress());
        for (INaviPacketListener listener : listeners) {
            listener.process(udpRequest, response);
            log.debug(listener.getClass().getName() + " is completed!");
            if (listener.isNotToNext()) {
                break;
            }
        }
        if (NaviDefine.TCP.equals(getProtocol())) {
            //有链接的，如果连接断开，先尝试连接3次
            if (null != channel && !channel.isConnected()) {
                int i = 0;
                while (!channel.isConnected() && i < 3) {
                    channel.connect(response.getRemoteAddress());
                    log.warn("reconnect channel : " + i);
                    i++;
                }
            }
            if (null != channel && channel.isConnected()) {
                channel.write(ChannelBuffers.wrappedBuffer(response.getResponse()), response.getRemoteAddress());
            } else {
                log.warn("channel is not conneted!");
            }
        } else {
            //无连接的，UDP
            if (null != channel && channel.isOpen() && null != response.getResponse()) {
                channel.write(ChannelBuffers.wrappedBuffer(response.getResponse()), response.getRemoteAddress());
            } else {
                //高并发存在效率问题，因为udp链接很容易断开，或者大多数情况不用返回数据
                //
//				log.warn("channel not open or no response data!");
            }
        }

    }

    public String getProtocol() {
        if (null != protocol) {
            return protocol;
        } else {
            protocol = ServerConfigure.get(NaviDefine.PROTOCOL);
            return protocol;
        }
    }

    /**
     * 注册请求处理监听
     *
     * @param listener
     */
    public void register(INaviPacketListener listener) {
        listeners.add(listener);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
        throws Exception {
        log.error(e.getCause().getMessage(), e.getCause());
    }
}
