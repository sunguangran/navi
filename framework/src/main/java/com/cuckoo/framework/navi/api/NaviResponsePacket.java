package com.cuckoo.framework.navi.api;

import lombok.Getter;
import lombok.Setter;

import java.net.SocketAddress;

@Setter
@Getter
public class NaviResponsePacket implements INaviResponsePacket {

    private SocketAddress remoteAddress;
    private byte[] responseByte;
    private String responseString;

    public byte[] getResponse() {
        if (null != responseByte) {
            return responseByte;
        } else if (null != responseString) {
            return responseString.getBytes();
        }
        
        return null;
    }
}
