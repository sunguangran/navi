package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.core.IBaseDataService;
import com.youku.java.navi.engine.core.INaviDataSource;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractNaviDataService implements IBaseDataService {

    protected INaviDataSource dataSource;

    public void afterPropertiesSet() throws Exception {
        if (dataSource == null) {
            throw new NaviSystemException("the dataSource is null", NaviError.SYSERROR);
        }
    }

}
