package com.youku.java.navi.engine.core;

import com.youku.java.navi.common.exception.NaviSystemException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;

public interface INaviHttp extends IBaseDataService {

    public String doGet(String uri) throws NaviSystemException;

    public String doPost(String uri, List<BasicNameValuePair> params)
        throws NaviSystemException;

    public String execute(HttpUriRequest request) throws NaviSystemException;

}
