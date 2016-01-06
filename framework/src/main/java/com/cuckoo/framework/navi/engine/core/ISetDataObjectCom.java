package com.cuckoo.framework.navi.engine.core;

import com.cuckoo.framework.navi.serviceobj.AbstractNaviDto;

import java.util.Set;

public interface ISetDataObjectCom<T extends AbstractNaviDto> extends IDataObjectCom<T> {
    int size();

    T randomMem();

    Set<T> getSetMems();

    Set<T> refreshSet();
}
