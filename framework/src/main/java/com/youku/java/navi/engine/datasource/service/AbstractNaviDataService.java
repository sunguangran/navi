package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.engine.core.IBaseDataService;
import com.youku.java.navi.engine.core.INaviDataSource;
import com.youku.java.navi.common.NAVIERROR;
import com.youku.java.navi.common.exception.NaviSystemException;


public abstract class AbstractNaviDataService implements IBaseDataService {

    protected INaviDataSource dataSource;

    public void setDataSource(INaviDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public INaviDataSource getDataSource() {
        return dataSource;
    }

    public void afterPropertiesSet() throws Exception {
        if (dataSource == null) {
            throw new NaviSystemException("the dataSource is null!",
                NAVIERROR.SYSERROR.code());
        }
    }

}
