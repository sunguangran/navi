package com.cuckoo.framework.navi.server.api;

import com.cuckoo.framework.navi.common.exception.NaviBusiException;

import java.util.List;

public interface INaviPacketInterrupter {

    void preAction(NaviRequestPacket packet, NaviResponsePacket response, List<NaviParameter> parameters) throws NaviBusiException;

    void postAction(NaviRequestPacket packet, NaviResponsePacket response) throws NaviBusiException;

}
