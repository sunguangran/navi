package com.cuckoo.framework.navi.server.handler;

import com.cuckoo.framework.navi.boot.NaviDefine;
import com.cuckoo.framework.navi.common.exception.NaviBusinessException;
import com.cuckoo.framework.navi.server.ServerConfigure;
import com.cuckoo.framework.navi.server.api.NaviRequestPacket;
import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;

import java.util.Arrays;

@Slf4j
public abstract class AbstractNaviPacketHandler extends SimpleChannelUpstreamHandler {
    protected String header_delimiter = null;
    protected String content_delimiter = null;
    protected int maxPacketSize = 0;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
        throws Exception {
        handle(decodeUDPPacket(e), e.getChannel());
        ctx.sendUpstream(e);
    }

    public NaviRequestPacket decodeUDPPacket(MessageEvent e) throws Exception {
        ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
        byte[] recByte = buffer.array();
        maxPacketSize = getMaxPacketSize();
        if (null == recByte || recByte.length < 1 || recByte.length > maxPacketSize) {
            throw new NaviBusinessException("invalid packet", -200);
        }
        header_delimiter = getHeaderDelimiter();
        content_delimiter = getContentDelimiter();
        if (null == header_delimiter || null == content_delimiter) {
            throw new NaviBusinessException("server delimiter can't be null", -200);
        }
        String header = getHeader(recByte, content_delimiter);
        byte[] content = getContent(recByte, content_delimiter);
        if (null == header || header.length() < 1
            || null == content || content.length < 1) {
            throw new NaviBusinessException("invalid packet", -200);
        }
        String[] headerMsg = header.split(header_delimiter);
        if (!(headerMsg.length == 4 || headerMsg.length == 3)) {
            throw new NaviBusinessException("wrong header format,invalid packet", -200);
        }
        NaviRequestPacket packet = null;
        if (headerMsg.length == 4) {
            packet = new NaviRequestPacket(headerMsg[0], headerMsg[1], headerMsg[2], headerMsg[3], content);
        } else {
            packet = new NaviRequestPacket(headerMsg[0], headerMsg[1], headerMsg[2], null, content);
        }
        packet.setRemoteAddress(e.getRemoteAddress());
        return packet;
    }

    public String getHeader(byte[] recByte, String delimiter) {
        if (null != recByte && null != delimiter && !"".equals(delimiter)
            && recByte.length > 0) {
            byte delimiterByte = delimiter.getBytes()[0];
            int end = -1;
            for (int i = 0; i < recByte.length; i++) {
                if (recByte[i] == delimiterByte) {
                    end = i;
                    break;
                }
            }
            if (end > -1) {
                return new String(Arrays.copyOf(recByte, end));
            }
        }
        return null;
    }

    public byte[] getContent(byte[] recByte, String delimiter) {
        if (null != recByte && null != delimiter && !"".equals(delimiter)
            && recByte.length > 0) {
            byte delimiterByte = delimiter.getBytes()[0];
            int end = -1;
            for (int i = 0; i < recByte.length; i++) {
                if (recByte[i] == delimiterByte) {
                    end = i;
                    break;
                }
            }
            if (end > -1) {
                return Arrays.copyOfRange(recByte, end + 1, recByte.length);
            }
        }
        return null;
    }

    public int getMaxPacketSize() {
        if (maxPacketSize > 0) {
            return maxPacketSize;
        }
        maxPacketSize = NaviDefine.DEFAULT_MAX_TCP_PACKET_SIZE;
        if (NaviDefine.TCP.equals(ServerConfigure.get(NaviDefine.PROTOCOL))) {
            if (null != ServerConfigure.get(NaviDefine.TCP_MAX_PACKET_SIZE)) {
                maxPacketSize = Integer.parseInt(ServerConfigure.get(NaviDefine.TCP_MAX_PACKET_SIZE));
            }
        } else if (NaviDefine.UDP.equals(ServerConfigure.get(NaviDefine.PROTOCOL))) {
            maxPacketSize = NaviDefine.DEFAULT_MAX_UDP_PACKET_SIZE;
            if (null != ServerConfigure.get(NaviDefine.UDP_MAX_PACKET_SIZE)) {
                maxPacketSize = Integer.parseInt(ServerConfigure.get(NaviDefine.UDP_MAX_PACKET_SIZE));
            }
        }
        return maxPacketSize;
    }

    public String getContentDelimiter() {
        if (null != content_delimiter) {
            return content_delimiter;
        }
        content_delimiter = NaviDefine.DEFAULT_CONTENT_DELIMITER;
        if (null != ServerConfigure.get(NaviDefine.CONTENT_DELIMITER)) {
            content_delimiter = ServerConfigure.get(NaviDefine.CONTENT_DELIMITER);
            if (null != content_delimiter && content_delimiter.contains("\\")) {
                content_delimiter = new String(new byte[]{Byte.parseByte(content_delimiter.replace("\\", ""))});
            }
        }
        return content_delimiter;
    }

    public String getHeaderDelimiter() {
        if (null != header_delimiter) {
            return header_delimiter;
        }
        header_delimiter = NaviDefine.DEFAULT_HEADER_DELIMITER;
        if (null != ServerConfigure.get(NaviDefine.HEADER_DELIMITER)) {
            header_delimiter = ServerConfigure.get(NaviDefine.HEADER_DELIMITER);
            if (null != header_delimiter && header_delimiter.contains("\\")) {
                header_delimiter = new String(new byte[]{Byte.parseByte(header_delimiter.replace("\\", ""))});
            }
        }
        return header_delimiter;
    }

    public abstract void handle(NaviRequestPacket udpRequest, Channel channel) throws Exception;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
        throws Exception {
        Throwable cause = ((ExceptionEvent) e).getCause();
        log.error(cause.toString(), cause);
        if (e.getFuture().getChannel().isOpen()) {
            e.getFuture().addListener(ChannelFutureListener.CLOSE);
        }
    }
}
