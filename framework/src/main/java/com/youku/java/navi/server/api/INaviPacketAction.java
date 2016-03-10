package com.youku.java.navi.server.api;

public interface INaviPacketAction {

    void action(com.youku.java.navi.server.api.NaviRequestPacket packet, com.youku.java.navi.server.api.NaviResponsePacket response) throws Exception;

}
