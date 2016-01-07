package com.cuckoo.framework.navi.server.api;

public interface INaviPacketAction {

    void action(NaviRequestPacket packet, NaviResponsePacket response) throws Exception;

}
