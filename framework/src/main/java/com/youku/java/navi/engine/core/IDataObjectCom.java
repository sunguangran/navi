package com.youku.java.navi.engine.core;

import com.youku.java.navi.server.serviceobj.AbstractNaviBaseDto;

public interface IDataObjectCom<T extends AbstractNaviBaseDto> {

    T get();

    T refresh();

    void deleteCache();

    void deleteDB();

    String getNullKey();

}
