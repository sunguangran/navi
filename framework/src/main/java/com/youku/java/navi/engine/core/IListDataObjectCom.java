package com.youku.java.navi.engine.core;

import com.youku.java.navi.server.serviceobj.AbstractNaviBaseDto;

import java.util.List;

public interface IListDataObjectCom<T extends AbstractNaviBaseDto> extends IDataObjectCom<T> {

    List<T> getListData();

    List<T> refreshList();

    int size();

    List<T> getIndex(int... indexs);

}
