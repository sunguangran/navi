package com.youku.java.navi.engine.core;

import org.springframework.beans.factory.InitializingBean;

public interface IBaseDataService extends InitializingBean {

    /**
     * 设置数据源
     *
     * @param dataSource
     */
    public void setDataSource(INaviDataSource dataSource);

    public INaviDataSource getDataSource();
}
