package com.cuckoo.framework.navi.engine.datasource.driver;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.cuckoo.framework.navi.engine.datasource.pool.NaviPoolConfig;
import com.cuckoo.framework.navi.serviceobj.MonitorReportObject;
import com.cuckoo.framework.navi.common.ServerAddress;

public class NaviMetricsMonitorDriver extends ANaviDriver {

    private MetricRegistry metrics = new MetricRegistry();
    private JmxReporter reporter;

    public NaviMetricsMonitorDriver(ServerAddress server, String auth, NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
        reporter = JmxReporter.forRegistry(metrics).build();
        reporter.start();
    }

    public void report(MonitorReportObject obj) {
        String service = obj.getService();
        String module = obj.getModule();
        String method = obj.getMethod();

        long cost = (long) (obj.getCost() * 1000);
        long s_in = obj.getReq_sz();
        long s_out = obj.getResp_sz();

        int code = obj.getCode();


        metrics.histogram(MetricRegistry.name(service, module, method, "cost")).update(cost);
        metrics.histogram(MetricRegistry.name(service, module, method, "size_input")).update(s_in);
        metrics.histogram(MetricRegistry.name(service, module, method, "size_output")).update(s_out);

        if (cost > getSlowQuery()) {
            metrics.meter(MetricRegistry.name(service, module, method, "slow_query")).mark();
        }

        if (s_out > getBigQuery()) {
            metrics.meter(MetricRegistry.name(service, module, method, "big_query")).mark();
        }

        metrics.meter(MetricRegistry.name(service, module, method, "req_total")).mark();

        if (code < 0) {
            metrics.meter(MetricRegistry.name(service, module, method, "req_failed")).mark();
        } else {
            metrics.meter(MetricRegistry.name(service, module, method, "req_succ")).mark();
        }
    }

    private long getSlowQuery() {
        return 200;
    }

    private long getBigQuery() {
        return 20000;
    }

    public void destroy() {
        if (reporter != null) {
            reporter.close();
        }
    }

    public boolean isAlive() {
        return false;
    }

    public boolean open() {
        return false;
    }

}
