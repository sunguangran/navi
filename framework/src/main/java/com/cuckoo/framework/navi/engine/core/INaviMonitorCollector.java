package com.cuckoo.framework.navi.engine.core;

import com.cuckoo.framework.navi.server.serviceobj.MonitorReportObject;

public interface INaviMonitorCollector extends IBaseDataService {

    boolean report(MonitorReportObject obj);

}
