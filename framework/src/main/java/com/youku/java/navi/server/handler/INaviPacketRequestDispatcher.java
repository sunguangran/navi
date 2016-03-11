package com.youku.java.navi.server.handler;

import com.youku.java.navi.server.api.NaviRequestPacket;
import com.youku.java.navi.server.api.NaviResponsePacket;

public interface INaviPacketRequestDispatcher {

    void process(NaviRequestPacket packet, NaviResponsePacket response) throws Exception;

}
