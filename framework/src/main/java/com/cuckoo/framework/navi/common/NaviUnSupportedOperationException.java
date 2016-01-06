package com.cuckoo.framework.navi.common;

public class NaviUnSupportedOperationException extends NaviSystemException {

    private static final long serialVersionUID = 1L;

    public NaviUnSupportedOperationException() {
        this("UnSupported Operation", NAVIERROR.SYSERROR.code());
    }

    public NaviUnSupportedOperationException(String desc, int code) {
        super(desc, code);
    }

}
