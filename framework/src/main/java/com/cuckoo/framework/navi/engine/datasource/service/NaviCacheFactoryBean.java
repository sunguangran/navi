package com.cuckoo.framework.navi.engine.datasource.service;

import com.cuckoo.framework.navi.engine.core.INaviCache;
import com.cuckoo.framework.navi.engine.core.INaviLog;
import com.cuckoo.framework.navi.engine.core.INaviMonitorCollector;
import org.springframework.beans.factory.FactoryBean;

/**
 * NaviCache工厂bean,用于将真实service包装成代理service
 */
public class NaviCacheFactoryBean implements FactoryBean<INaviCache> {

    private INaviCache realService;
    private boolean useproxy = true;
    private INaviMonitorCollector collector;
    private INaviLog log;

    public INaviCache getObject() throws Exception {
        if (useproxy) {
            NaviDataServiceProxy proxy = new NaviDataServiceProxy(realService, INaviCache.class);
            proxy.setCollector(collector);
            proxy.setLog(log);
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

    public void setRealService(INaviCache realService) {
        this.realService = realService;
    }

    public void setUseproxy(boolean useproxy) {
        this.useproxy = useproxy;
    }

    public void setCollector(INaviMonitorCollector collector) {
        this.collector = collector;
    }

    public void setLog(INaviLog log) {
        this.log = log;
    }

}
