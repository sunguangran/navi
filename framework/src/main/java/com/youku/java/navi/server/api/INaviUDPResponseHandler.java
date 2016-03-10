package com.youku.java.navi.server.api;

import java.io.Serializable;

public interface INaviUDPResponseHandler {

    <T extends Serializable> void handle(T obj);

    Class<? extends Serializable> getResponseClass();
}
