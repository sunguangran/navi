package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.core.INaviDriver;
import com.youku.java.navi.engine.core.INaviMonitorCollector;
import com.youku.java.navi.engine.datasource.driver.NaviMetricsMonitorDriver;
import com.youku.java.navi.server.serviceobj.MonitorReportObject;

public class NaviMetricsMonitorService extends AbstractNaviDataService implements INaviMonitorCollector {

    protected NaviMetricsMonitorDriver getDriver() {
        INaviDriver driver = dataSource.getHandle();
        if (driver instanceof NaviMetricsMonitorDriver) {
            return (NaviMetricsMonitorDriver) driver;
        }
        throw new NaviSystemException("the driver is invalid!", NaviError.SYSERROR);
    }

    public boolean report(MonitorReportObject obj) {
        getDriver().report(obj);
        return true;
    }

}
