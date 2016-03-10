package com.cuckoo.framework.navi.server.handler;

import com.cuckoo.framework.navi.api.NaviRequestPacket;
import com.cuckoo.framework.navi.api.NaviResponsePacket;

public interface INaviPacketListener {

    void process(NaviRequestPacket packet, NaviResponsePacket response) throws Exception;


    /**
     * 是否进行下个Listener的处理
     */
    boolean isNotToNext();
}
