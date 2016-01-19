package com.cuckoo.framework.navi.engine.core;

import com.cuckoo.framework.navi.serviceobj.AbstractNaviBean;

import java.util.Set;

public interface ISetDataObjectCom<T extends AbstractNaviBean> extends IDataObjectCom<T> {
    int size();

    T randomMem();

    Set<T> getSetMems();

    Set<T> refreshSet();
}
