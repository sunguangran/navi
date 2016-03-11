package com.youku.java.navi.server.serviceobj;

import com.youku.java.navi.engine.core.INaviCache;
import com.youku.java.navi.engine.core.INaviDB;
import com.youku.java.navi.engine.core.INaviZookeeper;

/**
 * 所有Dao必须实现的接口，以保证一个Dao操作一个Dto类
 *
 * @param <T>
 */
public interface INaviDao<T extends AbstractNaviDto> {

    /**
     * 获得Dao对应的Dto Class对象
     *
     * @return
     */
    public Class<T> getDtoClass();

    /**
     * 设置数据库服务
     *
     * @param dbService
     */
    public void setDbService(INaviDB dbService);

    /**
     * 设置缓存服务
     *
     * @param cacheService
     */
    public void setCacheService(INaviCache cacheService);

    /**
     * 设置协同服务
     *
     * @param csService
     */
    public void setCsService(INaviZookeeper csService);
}
