package com.cuckoo.framework.navi.server.handler;

import com.cuckoo.framework.navi.server.api.NaviRequestPacket;
import com.cuckoo.framework.navi.server.api.NaviResponsePacket;

public interface INaviPacketRequestDispatcher {

    void process(NaviRequestPacket packet, NaviResponsePacket response) throws Exception;

}
