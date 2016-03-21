package com.youku.java.navi.engine.core;

import org.springframework.beans.factory.InitializingBean;

public interface IBaseDataService extends InitializingBean {

    /**
     * 设置数据源
     *
     * @param dataSource
     */
    void setDataSource(INaviDataSource dataSource);

    INaviDataSource getDataSource();
}
