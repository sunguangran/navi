package com.youku.java.navi.engine.core;

import com.youku.java.navi.server.serviceobj.MonitorReportObject;

public interface INaviMonitorCollector extends IBaseDataService {
    public boolean report(MonitorReportObject obj);
}
