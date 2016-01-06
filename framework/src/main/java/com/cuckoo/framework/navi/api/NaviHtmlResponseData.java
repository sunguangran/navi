package com.cuckoo.framework.navi.api;

import com.cuckoo.framework.navi.common.NaviSystemException;


public class NaviHtmlResponseData extends AbstractNaviResponseData {

    public NaviHtmlResponseData(Object data) {
        super(data);
    }

    public String getResponseType() {
        return "text/html";
    }

    @Override
    public String toResponseNull() throws NaviSystemException {
        return null;
    }

    @Override
    public String toResponseForBusinessException() throws NaviSystemException {
        return null;
    }

    @Override
    public String toResponseForList() throws NaviSystemException {
        return null;
    }

    @Override
    public String toResponseForArray() throws NaviSystemException {
        return null;
    }

    @Override
    public String toResponseForObject() throws NaviSystemException {
        return "<html><body><b>" + data + "</b></body></html>";
    }

    public void setCost(long cost) {

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
