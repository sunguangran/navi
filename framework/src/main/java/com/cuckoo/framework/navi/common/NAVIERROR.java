package com.cuckoo.framework.navi.common;

public enum NaviError {

    SUCCESS(0),

    SYSERROR(-500),

    INVALID_HOST(-90),
    NO_DATA(-91),
    BUSI_PARAM_ERROR(-92);

    private int code;
    private String desc;

    NaviError(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    NaviError(int code) {
        this(code, "system error!");
    }

    public int code() {
        return code;
    }

    public String desc() {
        return desc;
    }
}
