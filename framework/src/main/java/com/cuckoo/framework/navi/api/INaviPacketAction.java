package com.cuckoo.framework.navi.api;

public interface INaviPacketAction {

    void action(NaviRequestPacket packet, NaviResponsePacket response) throws Exception;

}
