package com.cuckoo.framework.navi.engine.core;

import com.cuckoo.framework.navi.serviceobj.AbstractNaviDto;

public interface IDataObjectCom<T extends AbstractNaviDto> {
    T get();

    T refresh();

    void deleteCache();

    void deleteDB();

    String getNullKey();

}
