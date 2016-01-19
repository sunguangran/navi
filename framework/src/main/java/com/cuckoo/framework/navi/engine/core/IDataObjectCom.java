package com.cuckoo.framework.navi.engine.core;

import com.cuckoo.framework.navi.serviceobj.AbstractNaviBean;

public interface IDataObjectCom<T extends AbstractNaviBean> {
    T get();

    T refresh();

    void deleteCache();

    void deleteDB();

    String getNullKey();

}
