package com.youku.java.navi.server.api;

import com.youku.java.navi.common.exception.NaviBusinessException;

import java.util.List;

public interface INaviPacketInterrupter {

    void preAction(NaviRequestPacket packet, NaviResponsePacket response, List<NaviParamter> parameters) throws NaviBusinessException;

    void postAction(NaviRequestPacket packet, NaviResponsePacket response) throws NaviBusinessException;

}
