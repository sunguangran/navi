package com.cuckoo.framework.navi.common;

public enum NAVIERROR {

    SYSERROR(-500),
    INVALID_HOST(-90),
    BUSI_NO_DATA(-91),
    BUSI_PARAM_ERROR(-92);

    private int code;
    private String desc;

    private NAVIERROR(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private NAVIERROR(int code) {
        this(code, "system error!");
    }

    public int code() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
