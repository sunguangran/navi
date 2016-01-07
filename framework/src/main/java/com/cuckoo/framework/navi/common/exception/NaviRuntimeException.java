package com.cuckoo.framework.navi.common.exception;

import lombok.Getter;
import lombok.Setter;

public class NaviRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 2496795451361251951L;

    @Setter @Getter
    private int code;

    public NaviRuntimeException(int code, String desc) {
        this(code, desc, null);
    }

    public NaviRuntimeException(int code, String desc, Throwable t) {
        super(desc, t);
        this.code = code;

    }

    @Override
    public String toString() {
        return getMessage();
    }

}
