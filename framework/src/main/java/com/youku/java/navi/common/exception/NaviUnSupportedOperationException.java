package com.youku.java.navi.common.exception;

import com.youku.java.navi.common.NaviError;

public class NaviUnSupportedOperationException extends NaviSystemException {

    private static final long serialVersionUID = 1L;

    public NaviUnSupportedOperationException() {
        this("UnSupported Operation", NaviError.SYSERROR);
    }

    public NaviUnSupportedOperationException(String desc, int code) {
        super(desc, code);
    }

}
