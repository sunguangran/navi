package com.cuckoo.framework.navi.engine.core;

import com.cuckoo.framework.navi.serviceobj.AbstractNaviBean;

import java.util.List;

public interface IListDataObjectCom<T extends AbstractNaviBean> extends IDataObjectCom<T> {
    List<T> getListData();

    List<T> refreshList();

    int size();

    List<T> getIndex(int... indexs);
}
