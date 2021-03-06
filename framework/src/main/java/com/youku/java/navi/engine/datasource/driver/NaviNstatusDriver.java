package com.youku.java.navi.engine.datasource.driver;

import com.youku.java.navi.common.ServerUrlUtil;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.datasource.pool.NaviPoolConfig;
import com.youku.java.navi.engine.nstatus.JavaNstatusExt;
import com.youku.java.navi.server.serviceobj.MonitorReportObject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class NaviNstatusDriver extends AbstractNaviDriver implements Runnable {

    private BlockingQueue<MonitorReportObject> queue = new LinkedBlockingQueue<MonitorReportObject>();
    private Thread thread;
    private boolean running = true;
    private JavaNstatusExt nstatus;

    public NaviNstatusDriver(ServerUrlUtil.ServerUrl server, String auth,
                             NaviPoolConfig poolConfig) {
        super(server, auth, poolConfig);
        nstatus = new JavaNstatusExt(server.getUrl());
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("NstatusReportThread");
        thread.start();
    }

    public boolean addReportToQueue(MonitorReportObject obj) {
        return queue.add(obj);
    }

    public void destroy() {
        this.running = false;
        if (nstatus != null) {
            nstatus.destory();
        }
    }

    public boolean isAlive() {
        return true;
    }

    public boolean open() {
        return true;
    }

    @Override
    public void close() throws NaviSystemException {
        //donothing
    }

    public void run() {
        while (running) {
            MonitorReportObject obj = null;
            try {
                obj = queue.take();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            if (obj == null) {
                continue;
            }
            nstatus.report(obj.getService(), obj.getModule(), obj.getMethod(),
                obj.getXcaller(), obj.getRequest_ip(), obj.getCode(),
                obj.getCost(), obj.getReq_sz(), obj.getResp_sz());
        }
    }

}
