package com.cuckoo.framework.navi.engine.core;

import org.springframework.beans.factory.InitializingBean;

public interface IBaseDataService extends InitializingBean {

    /**
     * 设置数据源
     *
     * @param dataSource
     *     数据源
     */
    void setDataSource(INaviDataSource dataSource);

    INaviDataSource getDataSource();
}
