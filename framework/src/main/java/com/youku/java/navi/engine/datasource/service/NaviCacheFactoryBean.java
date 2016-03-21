package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.engine.core.INaviCache;
import com.youku.java.navi.engine.core.INaviLog;
import com.youku.java.navi.engine.core.INaviMonitorCollector;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;

/**
 * NaviCache工厂bean,用于将真实service包装成代理service
 */
public class NaviCacheFactoryBean implements FactoryBean<INaviCache> {

    @Setter
    private INaviCache realService;

    @Setter
    private boolean useproxy = true;

    @Setter
    private INaviMonitorCollector collector;

    @Setter
    private INaviLog log;

    public INaviCache getObject() throws Exception {
        if (useproxy) {
            NaviDataServiceProxy proxy = new NaviDataServiceProxy(realService, INaviCache.class);
            proxy.setCollector(collector);
            proxy.setNaviLog(log);
            return (INaviCache) proxy.getProxyService();
        } else {
            return realService;
        }
    }

    public Class<?> getObjectType() {
        return INaviCache.class;
    }

    public boolean isSingleton() {
        return true;
    }

}
