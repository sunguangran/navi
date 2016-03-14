package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.core.INaviDriver;
import com.youku.java.navi.engine.core.INaviMonitorCollector;
import com.youku.java.navi.engine.datasource.driver.NaviNstatusDriver;
import com.youku.java.navi.server.serviceobj.MonitorReportObject;

public class NaviNstatusService extends AbstractNaviDataService implements INaviMonitorCollector {

    protected NaviNstatusDriver getDriver() {
        INaviDriver driver = dataSource.getHandle();
        if (driver instanceof NaviNstatusDriver) {
            return (NaviNstatusDriver) driver;
        }
        throw new NaviSystemException("the driver is invalid!",
            NaviError.SYSERROR);
    }

    public boolean report(MonitorReportObject obj) {
        return getDriver().addReportToQueue(obj);
    }

}
