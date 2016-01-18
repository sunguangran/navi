package com.cuckoo.framework.navi.common.exception;

import com.cuckoo.framework.navi.common.NaviError;

public class NaviUnSupportedOperationException extends NaviSystemException {

    private static final long serialVersionUID = 1L;

    public NaviUnSupportedOperationException() {
        this("operation unsupported", NaviError.SYSERROR.code());
    }

    public NaviUnSupportedOperationException(String desc, int code) {
        super(desc, code);
    }

}
