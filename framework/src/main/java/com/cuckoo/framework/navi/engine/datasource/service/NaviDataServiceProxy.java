package com.cuckoo.framework.navi.engine.datasource.service;

import com.cuckoo.framework.navi.engine.core.INaviMonitorCollector;
import com.cuckoo.framework.navi.engine.core.IBaseDataService;
import com.cuckoo.framework.navi.engine.core.INaviLog;
import com.cuckoo.framework.navi.server.ServerConfigure;
import com.cuckoo.framework.navi.serviceobj.MonitorReportObject;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * DataService的动态代理类
 *
 */
@Slf4j
public class NaviDataServiceProxy implements MethodInterceptor, InvocationHandler {

    private IBaseDataService realService;
    private Object proxyService;
    private String localhost;
    private INaviMonitorCollector collector;
    private INaviLog naviLog;

    public NaviDataServiceProxy(IBaseDataService realService, Class<?> inter) {
        this.realService = realService;
        Class<?>[] inters = realService.getClass().getInterfaces();
        //直接实现目标的接口的情况下使用java原生动态代理,否则使用cglib
        if (find(inters, inter)) {
            this.proxyService = Proxy.newProxyInstance(realService.getClass()
                    .getClassLoader(), realService.getClass().getInterfaces(),
                this);
        } else {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(this.realService.getClass());
            // 回调方法
            enhancer.setCallback(this);
            // 创建代理对象
            this.proxyService = enhancer.create();
        }
    }

    public Object getProxyService() {
        return proxyService;
    }

    private boolean find(Class<?>[] inters, Class<?> inter) {
        for (Class<?> i : inters) {
            if (i == inter) {
                return true;
            }
        }
        return false;
    }

    public Object intercept(Object proxyObject, Method method, Object[] args,
                            MethodProxy proxy) throws Throwable {
        return invoke(proxyObject, method, args);
    }

    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = method.invoke(realService, args);
            monitor(method.getName(), start, 0);
            return result;
        } catch (InvocationTargetException e) {
            monitor(method.getName(), start, -1);
            log();
            throw e.getCause();
        }
    }

    private void log() {
        if (naviLog == null) {
            return;
        }
    }

    private void monitor(String api, long start, int code) {
        if (collector == null) {
            return;
        }
        String collectName = realService.getDataSource().getNamespace();
        if (StringUtils.isEmpty(collectName)) {
            collectName = realService.getClass().getSimpleName();
        }
        long cost = System.currentTimeMillis() - start;
        double dcost = cost * 1.00d / 1000d;
        MonitorReportObject obj = new MonitorReportObject();
        obj.setCode(code);
        obj.setCost(dcost);
        obj.setMethod(collectName + ":" + api);
        obj.setModule(collectName);
        obj.setReq_sz(0);
        obj.setRequest_ip(getLocalhost());
        obj.setService(ServerConfigure.getServer());
        obj.setXcaller("");
        obj.setResp_sz(0);
        collector.report(obj);
    }

    private String getLocalhost() {
        if (localhost == null) {
            synchronized (this) {
                if (localhost == null) {
                    try {
                        InetAddress addr = InetAddress.getLocalHost();
                        byte[] ipAddr = addr.getAddress();
                        StringBuilder ipAddrStr = new StringBuilder("");
                        for (int i = 0; i < ipAddr.length; i++) {
                            if (i > 0) {
                                ipAddrStr.append(".");
                            }
                            ipAddrStr.append(ipAddr[i] & 0xFF);
                        }
                        localhost = ipAddrStr.toString();
                    } catch (UnknownHostException e) {
                        log.error("{}", e.getMessage());
                    }

                }
            }
        }
        return localhost;
    }

    public void setCollector(INaviMonitorCollector collector) {
        this.collector = collector;
    }

    public void setLog(INaviLog log) {
        this.naviLog = log;
    }

}
