package com.youku.java.navi.engine.core;

import com.youku.java.navi.server.serviceobj.AbstractNaviDto;

import java.util.Set;

public interface ISetDataObjectCom<T extends AbstractNaviDto> extends IDataObjectCom<T> {
    int size();

    T randomMem();

    Set<T> getSetMems();

    Set<T> refreshSet();
}
