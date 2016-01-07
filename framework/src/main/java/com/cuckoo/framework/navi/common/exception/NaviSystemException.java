package com.cuckoo.framework.navi.common.exception;

public class NaviSystemException extends NaviRuntimeException {

    private static final long serialVersionUID = -4375623832613058227L;

    public NaviSystemException(String desc, int code) {
        super(code, desc);
    }

    public NaviSystemException(String desc, int code, Throwable t) {
        super(code, desc, t);
    }
}
