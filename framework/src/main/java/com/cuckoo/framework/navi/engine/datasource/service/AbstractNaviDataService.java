package com.cuckoo.framework.navi.engine.datasource.service;

import com.cuckoo.framework.navi.engine.core.IBaseDataService;
import com.cuckoo.framework.navi.engine.core.INaviDataSource;
import com.cuckoo.framework.navi.common.NAVIERROR;
import com.cuckoo.framework.navi.common.NaviSystemException;


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
