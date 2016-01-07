package com.cuckoo.framework.navi.server.handler;

import com.cuckoo.framework.navi.server.api.NaviRequestPacket;
import com.cuckoo.framework.navi.server.api.NaviResponsePacket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BadPacketRequestListener implements INaviPacketListener {
    private boolean notToNext = false;

    public void process(NaviRequestPacket packet, NaviResponsePacket response)
        throws Exception {
        if (null == packet.getRequestService() || null == packet.getRequestModule()
            || null == packet.getRequestApi()) {
            notToNext = true;
        }
        if (null == packet.getRequestService()) {
            log.error("udp request: service name can't be null!");
        }
        if (null == packet.getRequestModule()) {
            log.error("udp request: module name can't be null!");
        }
        if (null == packet.getRequestApi()) {
            log.error("udp request: api name can't be null!");
        }
    }

    public boolean isNotToNext() {
        return notToNext;
    }

    public void setNotToNext(boolean notToNext) {
        this.notToNext = notToNext;
    }
}
