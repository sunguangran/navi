package com.cuckoo.framework.navi.engine.datasource;

import com.cuckoo.framework.navi.engine.core.INaviDataSource;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class AbstractNaviDataSource implements INaviDataSource {

    protected String namespace;
    protected String offlineConnectString;
    protected String deployConnectString;
    protected String type;
    protected String workMode;
    protected long slowQuery;
    protected String auth;

}
