package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.engine.core.INaviMonitorCollector;
import com.youku.java.navi.engine.core.INaviDB;
import com.youku.java.navi.engine.core.INaviLog;
import org.springframework.beans.factory.FactoryBean;

/**
 * NaviDB工厂bean,用于将真实service包装成代理service
 *
 */
public class NaviDBFactoryBean implements FactoryBean<INaviDB> {

    private INaviDB realService;
    private boolean useproxy = true;
    private INaviMonitorCollector collector;
    private INaviLog log;

    public INaviDB getObject() throws Exception {
        if (useproxy) {
            NaviDataServiceProxy proxy = new NaviDataServiceProxy(realService, INaviDB.class);
            proxy.setCollector(collector);
            proxy.setLog(log);
            return (INaviDB) proxy.getProxyService();
        } else {
            return realService;
        }
    }

    public Class<?> getObjectType() {
        return INaviDB.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setRealService(INaviDB realService) {
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
