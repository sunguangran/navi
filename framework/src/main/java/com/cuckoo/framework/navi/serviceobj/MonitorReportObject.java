package com.cuckoo.framework.navi.serviceobj;

public class MonitorReportObject {

    private String service = "";
    private String module = "";
    private String method = "";
    private String xcaller = "";
    private String request_ip = "";
    private int code = 0;
    private double cost = 0.0;
    private int req_sz = 0;
    private int resp_sz = 0;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getXcaller() {
        return xcaller;
    }

    public void setXcaller(String xcaller) {
        this.xcaller = xcaller;
    }

    public String getRequest_ip() {
        return request_ip;
    }

    public void setRequest_ip(String request_ip) {
        this.request_ip = request_ip;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getReq_sz() {
        return req_sz;
    }

    public void setReq_sz(int req_sz) {
        this.req_sz = req_sz;
    }

    public int getResp_sz() {
        return resp_sz;
    }

    public void setResp_sz(int resp_sz) {
        this.resp_sz = resp_sz;
    }
}
