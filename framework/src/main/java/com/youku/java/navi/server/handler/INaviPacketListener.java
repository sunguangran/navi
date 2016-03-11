package com.youku.java.navi.server.handler;

import com.youku.java.navi.server.api.NaviRequestPacket;
import com.youku.java.navi.server.api.NaviResponsePacket;

public interface INaviPacketListener {

    void process(NaviRequestPacket packet, NaviResponsePacket response) throws Exception;


    /**
     * 是否进行下个Listener的处理
     */
    boolean isNotToNext();
}
