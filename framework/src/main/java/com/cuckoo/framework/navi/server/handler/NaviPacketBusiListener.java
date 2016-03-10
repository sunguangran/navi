package com.cuckoo.framework.navi.server.handler;

import com.cuckoo.framework.navi.api.NaviRequestPacket;
import com.cuckoo.framework.navi.api.NaviResponsePacket;

public class NaviPacketBusiListener implements INaviPacketListener {
    private INaviPacketRequestDispatcher naviDispatcher;

    public NaviPacketBusiListener() {
        this.naviDispatcher = new DefaultNaviPacketDispatcher();
    }

    public void process(NaviRequestPacket packet, NaviResponsePacket response)
        throws Exception {
        naviDispatcher.process(packet, response);
    }

    public boolean isNotToNext() {
        return false;
    }

}
