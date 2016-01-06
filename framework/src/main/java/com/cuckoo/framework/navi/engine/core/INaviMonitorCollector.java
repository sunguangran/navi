package com.cuckoo.framework.navi.engine.core;

import com.cuckoo.framework.navi.serviceobj.MonitorReportObject;

public interface INaviMonitorCollector extends IBaseDataService {
    public boolean report(MonitorReportObject obj);
}
