package com.cuckoo.framework.navi.engine.core;

import com.cuckoo.framework.navi.common.exception.NaviSystemException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;

public interface INaviHttp extends IBaseDataService {

    String doGet(String uri) throws NaviSystemException;

    String doPost(String uri, List<BasicNameValuePair> params) throws NaviSystemException;

    String execute(HttpUriRequest request) throws NaviSystemException;

}
