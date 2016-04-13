package com.youku.java.navi.engine.async;


public class NaviAsyncRetryException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private int times = 0;

    public NaviAsyncRetryException(Throwable e, int times) {
        this.initCause(e);
        this.times = times;
    }

    public int getTimes() {
        return times;
    }
}
