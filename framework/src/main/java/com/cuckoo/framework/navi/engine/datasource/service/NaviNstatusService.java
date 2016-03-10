package com.cuckoo.framework.navi.engine.datasource.service;

import com.cuckoo.framework.navi.engine.core.INaviDriver;
import com.cuckoo.framework.navi.engine.core.INaviMonitorCollector;
import com.cuckoo.framework.navi.engine.datasource.driver.NaviNstatusDriver;
import com.cuckoo.framework.navi.common.NAVIERROR;
import com.cuckoo.framework.navi.common.NaviSystemException;
import com.cuckoo.framework.navi.serviceobj.MonitorReportObject;

public class NaviNstatusService extends AbstractNaviDataService implements INaviMonitorCollector {

    protected NaviNstatusDriver getDriver() {
        INaviDriver driver = dataSource.getHandle();
        if (driver instanceof NaviNstatusDriver) {
            return (NaviNstatusDriver) driver;
        }
        throw new NaviSystemException("the driver is invalid!",
            NAVIERROR.SYSERROR.code());
    }

    public boolean report(MonitorReportObject obj) {
        return getDriver().addReportToQueue(obj);
    }

}
