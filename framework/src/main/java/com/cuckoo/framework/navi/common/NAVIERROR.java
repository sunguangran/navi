package com.cuckoo.framework.navi.common;

public enum NaviError {

    SUCCESS(0),
    FAILED(-1),

    NO_DATA(-102),
    ACTION_UNKNOWN(404),
    SYSERROR(-500),
    HOST_INVALID(-90),

    PARAM_ERROR(-1001);

    /***************************************************/
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
