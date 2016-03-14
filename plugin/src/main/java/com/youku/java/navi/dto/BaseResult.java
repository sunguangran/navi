package com.youku.java.navi.dto;

import com.youku.java.navi.common.NaviError;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BaseResult<T> {

    private int code = NaviError.ACTION_FAILED;
    private String msg;
    private T data;
    private long total;

    private Integer intCode;

    public BaseResult() {
        this(NaviError.ACTION_FAILED);
    }

    public BaseResult(int code) {
        this.code = code;
    }

    public boolean success() {
        return this.code == NaviError.ACTION_SUCCED;
    }

    public int code() {
        return this.getCode();
    }

    public String msg() {
        return this.getMsg();
    }

    public BaseResult<T> makeSuccess() {
        this.code = NaviError.ACTION_SUCCED;
        return this;
    }

}
