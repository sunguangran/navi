package com.cuckoo.framework.navi.engine.datasource.service;

import com.cuckoo.framework.navi.common.NaviError;
import com.cuckoo.framework.navi.common.exception.NaviSystemException;
import com.cuckoo.framework.navi.engine.core.INaviDriver;
import com.cuckoo.framework.navi.engine.core.INaviMonitorCollector;
import com.cuckoo.framework.navi.engine.datasource.driver.NaviMetricsMonitorDriver;
import com.cuckoo.framework.navi.serviceobj.MonitorReportObject;

public class NaviMetricsMonitorService extends AbstractNaviDataService implements INaviMonitorCollector {

    protected NaviMetricsMonitorDriver getDriver() {
        INaviDriver driver = dataSource.getHandle();
        if (driver instanceof NaviMetricsMonitorDriver) {
            return (NaviMetricsMonitorDriver) driver;
        }
        throw new NaviSystemException("the driver is invalid!",
            NaviError.SYSERROR.code());
    }

    public boolean report(MonitorReportObject obj) {
        getDriver().report(obj);
        return true;
    }

}
