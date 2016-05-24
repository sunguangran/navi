package com.youku.java.navi.server.serviceobj;

import lombok.extern.slf4j.Slf4j;

/**
 * navi下dto需继承该超类 该类提供辅助性工具
 */
@Deprecated
@Slf4j
public abstract class AbstractNaviDto extends AbstractNaviBaseDto<Long>{

    public AbstractNaviDto() {
        super();
    }

}
