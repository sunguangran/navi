package com.cuckoo.framework.navi.api;

import com.cuckoo.framework.navi.annotation.Param;
import com.cuckoo.framework.navi.annotation.Rest;
import com.cuckoo.framework.navi.common.NaviRuntimeException;
import com.cuckoo.framework.navi.common.RestApi;
import com.cuckoo.framework.navi.engine.core.INaviMonitorCollector;
import com.cuckoo.framework.navi.module.NaviModuleContextFactory;
import com.cuckoo.framework.navi.server.ServerConfigure;
import com.cuckoo.framework.navi.serviceobj.MonitorReportObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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

            Object[] args = new Object[method.getParameterCount()];

            for (int i = 0; i < method.getParameterCount(); i++) {
                Parameter param = method.getParameters()[i];
                if (param.getType().equals(NaviHttpRequest.class)) {
                    args[i] = request;
                } else if (param.getType().equals(NaviHttpResponse.class)) {
                    args[i] = response;
                } else {
                    Param an = param.getAnnotation(Param.class);
                    boolean empty = StringUtils.isEmpty(request.getParameter(an.value()));

                    Class<?> type = param.getType();

                    Object value;
                    if (type.equals(Integer.class) || type.equals(int.class)) {
                        value = empty ? 0 : Integer.parseInt(request.getParameter(an.value()));
                    } else if (type.equals(Long.class) || type.equals(long.class)) {
                        value = empty ? 0L : Long.parseLong(request.getParameter(an.value()));
                    } else if (type.equals(Float.class) || type.equals(float.class)) {
                        value = empty ? 0F : Float.parseFloat(request.getParameter(an.value()));
                    } else if (type.equals(Double.class) || type.equals(double.class)) {
                        value = empty ? 0D :Double.parseDouble(request.getParameter(an.value()));
                    } else {
                        value = empty ? null : request.getParameter(an.value());
                    }

                    args[i] = value;
                }
            }

            // 处理
            Object res = method.invoke(this, args);

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
            String[] typeAns = typeAn.value().split("/");
            if (typeAns.length < 2) {
                return;
            }

            String moduleNm = typeAns[1];
            for (Method method : this.getClass().getDeclaredMethods()) {
                Rest methodAn = method.getAnnotation(Rest.class);
                if (methodAn != null) {
                    String uri = "/" + ServerConfigure.getServer() + typeAn.value() + methodAn.value();

                    RestApi api = new RestApi();
                    api.setModuleNm(moduleNm);
                    api.setUri(uri);
                    api.setClazz(this.getClass());
                    api.setMethod(method);

                    NaviModuleContextFactory.getInstance().addRestApi(uri, api);

                    log.info("rest api found: " + uri);
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

        for (INaviInterrupter interrupter : interrupters) {
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

        for (INaviInterrupter interrupter : interrupters) {
            if (!interrupter.preAction(request, response, parameters)) {
                return false;
            }
        }

        return true;
    }

}
