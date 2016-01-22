package com.cuckoo.framework.navi.server.serviceobj;

import com.cuckoo.framework.navi.engine.core.INaviCache;
import com.cuckoo.framework.navi.engine.core.INaviDB;
import com.cuckoo.framework.navi.engine.core.INaviZookeeper;

/**
 * 所有Dao必须实现的接口，以保证一个Dao操作一个Dto类
 *
 * @param <T>
 */
public interface INaviDao<T extends AbstractNaviBean> {

    /**
     * 获得Dao对应的Dto Class对象
     *
     */
    Class<T> getDtoClass();

    /**
     * 设置数据库服务
     *
     */
    void setDbService(INaviDB dbService);

    /**
     * 设置缓存服务
     *
     */
    void setCacheService(INaviCache cacheService);

    /**
     * 设置协同服务
     *
     */
    void setCsService(INaviZookeeper csService);
}
