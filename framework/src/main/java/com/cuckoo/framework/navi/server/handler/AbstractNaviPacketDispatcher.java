package com.cuckoo.framework.navi.server.handler;

import com.cuckoo.framework.navi.common.exception.NaviBusiException;
import com.cuckoo.framework.navi.server.api.NaviRequestPacket;
import com.cuckoo.framework.navi.server.api.NaviResponsePacket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractNaviPacketDispatcher implements
    INaviPacketRequestDispatcher {

    public abstract void callApi(NaviRequestPacket packet, NaviResponsePacket response) throws Exception;

    public void process(NaviRequestPacket packet, NaviResponsePacket response)
        throws Exception {
        try {
            if (packet != null) {
                callApi(packet, response);
            } else {
                log.warn("udp request is null!");
            }
        } catch (Exception e) {
            if (e instanceof NaviBusiException) {
                response.setResponseString("handle error");
            }
            throw e;
        }

    }

}
