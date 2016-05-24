package com.youku.java.navi.dao;

import com.youku.java.navi.engine.core.INaviCache;
import com.youku.java.navi.engine.core.INaviDB;
import com.youku.java.navi.server.serviceobj.AbstractNaviDto;
import lombok.extern.slf4j.Slf4j;

/**
 * basedao类,主键类型为Long
 *
 */
@Deprecated
@Slf4j
public abstract class BaseDao<T extends AbstractNaviDto> extends ABaseDao<T, Long> {

    /**
     * 不建议使用,如果使用此方式构造,需要子类自己实现将对应的service注入到父类的逻辑
     */
    @Deprecated
    protected BaseDao(Class<T> classNm) {
        super(classNm);
    }

    /**
     * 使用构造方法注入相关服务
     */
    protected BaseDao(Class<T> classNm, INaviDB dbService, INaviCache cacheService, AutoIncrDao autoIncrDao) {
        super(classNm, dbService, cacheService, autoIncrDao);
    }

}
