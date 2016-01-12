package com.cuckoo.framework.navi.engine.datasource.service;

import com.cuckoo.framework.navi.engine.core.INaviDB;
import com.cuckoo.framework.navi.engine.core.INaviLog;
import com.cuckoo.framework.navi.engine.core.INaviMonitorCollector;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;

/**
 * NaviDB工厂bean,用于将真实service包装成代理service
 */
@Setter
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

}
