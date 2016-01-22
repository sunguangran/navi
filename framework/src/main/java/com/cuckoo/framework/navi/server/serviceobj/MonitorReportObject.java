package com.cuckoo.framework.navi.server.serviceobj;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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

}
