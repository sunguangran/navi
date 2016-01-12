package com.cuckoo.framework.navi.server.api;

import com.cuckoo.framework.navi.common.exception.NaviRuntimeException;
import com.cuckoo.framework.navi.engine.core.INaviMonitorCollector;
import com.cuckoo.framework.navi.serviceobj.MonitorReportObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public abstract class ANaviPacketAction implements INaviPacketAction {

    private List<INaviPacketInterrupter> interrupters = null;
    private List<NaviParameter> parameters;
    private INaviMonitorCollector collector;

    public void action(NaviRequestPacket request, NaviResponsePacket response) throws Exception {
        long start = System.currentTimeMillis();
        // 前置操作
        preAction(request, response);
        // 处理
        try {
            doAction(request, response);
        } catch (Exception e) {
            if (e instanceof NaviRuntimeException) {
                monitor(request, response, start, ((NaviRuntimeException) e).getCode());
            } else {
                monitor(request, response, start, -500);
            }

            this.errorAction(request, response, e);
            throw e;
        }
        // 后置操作
        postAction(request, response);

        monitor(request, response, start, 0);
    }

    private void monitor(NaviRequestPacket request, NaviResponsePacket response, long start, int code) {
        if (collector == null) {
            return;
        }
        long cost = System.currentTimeMillis() - start;
        double dcost = cost * 1.00d / 1000d;
        MonitorReportObject obj = new MonitorReportObject();
        obj.setCode(code);
        obj.setCost(dcost);
        obj.setMethod(request.getRequestModule() + ":" + request.getRequestApi());
        obj.setModule(request.getRequestModule());
        obj.setReq_sz(request.size());
        obj.setRequest_ip(response.getRemoteAddress().toString());
        obj.setService(request.getRequestService());
        obj.setXcaller(request.getExtraMsg());
        obj.setResp_sz(null != response.getResponse() ? response.getResponse().length : 0);
        collector.report(obj);
    }

    protected void errorAction(NaviRequestPacket packet, NaviResponsePacket response, Throwable e) throws Exception {
    }

    protected void preAction(NaviRequestPacket packet, NaviResponsePacket response) throws Exception {
        if (interrupters == null) {
            return;
        }

        for (INaviPacketInterrupter interrupter : interrupters) {
            interrupter.preAction(packet, response, parameters);
        }
    }

    protected void postAction(NaviRequestPacket packet, NaviResponsePacket response) throws Exception {
        if (interrupters == null) {
            return;
        }

        for (INaviPacketInterrupter interrupter : interrupters) {
            interrupter.postAction(packet, response);
        }
    }

    public abstract void doAction(NaviRequestPacket packet, NaviResponsePacket response) throws Exception;

}
