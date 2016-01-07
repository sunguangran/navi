package com.cuckoo.framework.navi.server.api;

import com.cuckoo.framework.navi.common.exception.NaviBusinessException;

import java.util.List;

public interface INaviPacketInterrupter {

    void preAction(NaviRequestPacket packet, NaviResponsePacket response, List<NaviParamter> parameters) throws NaviBusinessException;

    void postAction(NaviRequestPacket packet, NaviResponsePacket response) throws NaviBusinessException;

}
