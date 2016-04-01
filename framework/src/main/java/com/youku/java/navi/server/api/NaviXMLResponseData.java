package com.youku.java.navi.server.api;

import com.alibaba.fastjson.JSONException;
import com.youku.java.navi.common.exception.NaviSystemException;

public class NaviXMLResponseData extends ANaviResponseData {

    public NaviXMLResponseData(Object data) {
        super(data);
    }

    @Override
    protected String toJsonData(Object data, String provider, String desc, int code) throws JSONException {
        return null;
    }

    public String getResponseType() {
        return "text/xml;charset=UTF-8";
    }

    @Override
    public String toResponseNull() throws NaviSystemException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toResponseForBusinessException() throws NaviSystemException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toResponseForList() throws NaviSystemException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toResponseForArray() throws NaviSystemException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toResponseForObject() throws NaviSystemException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String toResponseForJsonArray() throws NaviSystemException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String toResponseForJsonObject() throws NaviSystemException {
        // TODO Auto-generated method stub
        return null;
    }


}
