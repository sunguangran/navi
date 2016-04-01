package com.youku.java.navi.server.api;

import com.youku.java.navi.common.Rest;
import com.youku.java.navi.common.RestApi;
import com.youku.java.navi.common.exception.NaviRuntimeException;
import com.youku.java.navi.engine.core.INaviMonitorCollector;
import com.youku.java.navi.server.module.NaviModuleContextFactory;
import com.youku.java.navi.server.serviceobj.MonitorReportObject;
import com.youku.java.navi.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Method;
import java.util.List;

@Slf4j
@Setter
@Getter
public abstract class ANaviAction implements InitializingBean {

    private List<INaviInterrupter> interrupters = null;
    private List<NaviParamter> parameters;

    private INaviMonitorCollector collector;

    public void action(NaviHttpRequest request, NaviHttpResponse response, Method method) throws Exception {
        long start = System.currentTimeMillis();

        try {
            // 前置操作
            if (!preAction(request, response)) {
                return;
            }

            Object[] args = new Object[]{request, response};

            // action
            method.invoke(this, args);

            // 后置操作
            postAction(request, response);
        } catch (Exception e) {
            if (e instanceof NaviRuntimeException) {
                monitor(request, response, start, ((NaviRuntimeException) e).getCode());
            } else {
                monitor(request, response, start, -500);
            }

            error(request, response, e);
            throw e;
        } finally {
            monitor(request, response, start, 0);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Rest typeAn = this.getClass().getAnnotation(Rest.class);
        if (typeAn != null) {
            String typeRest = StringUtils.isEmpty(typeAn.value()) ? this.getClass().getSimpleName().toLowerCase() : typeAn.value();
            for (Method method : this.getClass().getDeclaredMethods()) {
                Rest methodRest = method.getAnnotation(Rest.class);
                if (methodRest != null) {
                    String uri = "/" + typeRest + (StringUtils.isEmpty(methodRest.value()) ? "/" + method.getName() + ".json" : "/" + methodRest.value()).trim();
                    if (!uri.endsWith(".json")) {
                        uri += ".json";
                    }

                    uri = uri.replaceAll("/(/)+", "/");

                    RestApi api = new RestApi();
                    api.setUri(uri);
                    api.setClazz(this.getClass());
                    api.setMethod(method);

                    NaviModuleContextFactory.getInstance().addRestApi(uri, api);

                    log.debug("rest api found: " + uri);
                }
            }
        }
    }

    private void monitor(NaviHttpRequest request, NaviHttpResponse response, long start, int code) {
        if (collector == null) {
            return;
        }

        long cost = System.currentTimeMillis() - start;
        double dcost = cost * 1.00d / 1000d;
        String xcaller = "";
        for (int i = 0; i < request.getHeaders().size(); i++) {
            if ("xcaller".equals(request.getHeaders().get(i).getKey())) {
                xcaller = request.getHeaders().get(i).getValue();
            }
        }

        MonitorReportObject obj = new MonitorReportObject();
        obj.setCode(code);
        obj.setCost(dcost);
        obj.setMethod(request.getModuleNm() + ":" + request.getUri());
        obj.setModule(request.getModuleNm());
        obj.setReq_sz(request.toString().length());
        obj.setRequest_ip(request.getClientIP());
        obj.setService(request.getServer());
        obj.setXcaller(xcaller);
        obj.setResp_sz(response.toString().length());
        collector.report(obj);
    }

    /**
     * 异常处理方法
     */
    protected void error(NaviHttpRequest request, NaviHttpResponse response, Throwable e) throws Exception {

    }

    protected boolean postAction(NaviHttpRequest request, NaviHttpResponse response) throws Exception {
        if (interrupters == null) {
            return true;
        }

        for (com.youku.java.navi.server.api.INaviInterrupter interrupter : interrupters) {
            if (!interrupter.postAction(request, response)) {
                return false;
            }
        }

        return true;
    }

    protected boolean preAction(NaviHttpRequest request, NaviHttpResponse response) throws Exception {
        if (interrupters == null) {
            return true;
        }

        for (com.youku.java.navi.server.api.INaviInterrupter interrupter : interrupters) {
            if (!interrupter.preAction(request, response, parameters)) {
                return false;
            }
        }

        return true;
    }

}
