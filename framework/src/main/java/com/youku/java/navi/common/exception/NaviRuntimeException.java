package com.youku.java.navi.common.exception;

import lombok.Getter;
import lombok.Setter;

public class NaviRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -5821255308346220789L;

    @Setter @Getter
    private int code;

    public NaviRuntimeException(String desc, int code) {
        this(desc, code, null);
    }

    public NaviRuntimeException(String desc, int code, Throwable t) {
        super(desc, t);
        this.code = code;
    }

    @Override
    public String toString() {
        return getMessage();
    }

}
