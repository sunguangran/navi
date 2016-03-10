package com.youku.java.navi.server.handler;

import com.youku.java.navi.server.api.NaviRequestPacket;
import com.youku.java.navi.server.api.NaviResponsePacket;

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
