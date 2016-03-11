package com.youku.java.navi.engine.core;

import java.io.Serializable;


public interface INaviUDPClientQueueCom {
    public <T extends Serializable> void sendQueue(T msg);

}
