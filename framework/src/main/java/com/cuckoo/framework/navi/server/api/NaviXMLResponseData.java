package com.cuckoo.framework.navi.server.api;

import com.cuckoo.framework.navi.common.exception.NaviSystemException;

public class NaviXMLResponseData extends ANaviResponseData {

    public NaviXMLResponseData(Object data) {
        super(data);
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
