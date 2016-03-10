package com.cuckoo.framework.navi.api;

import java.io.Serializable;

public interface INaviUDPResponseHandler {

    <T extends Serializable> void handle(T obj);

    Class<? extends Serializable> getResponseClass();
}
