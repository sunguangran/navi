package com.cuckoo.framework.navi.server.handler;

import com.cuckoo.framework.navi.api.NaviRequestPacket;
import com.cuckoo.framework.navi.api.NaviResponsePacket;

public interface INaviPacketRequestDispatcher {

    void process(NaviRequestPacket packet, NaviResponsePacket response) throws Exception;

}
