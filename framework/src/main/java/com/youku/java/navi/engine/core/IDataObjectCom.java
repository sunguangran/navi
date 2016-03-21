package com.youku.java.navi.engine.core;

import com.youku.java.navi.server.serviceobj.AbstractNaviDto;

public interface IDataObjectCom<T extends AbstractNaviDto> {

    T get();

    T refresh();

    void deleteCache();

    void deleteDB();

    String getNullKey();

}
