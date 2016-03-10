package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.engine.datasource.driver.NaviHttpClientDriver;
import com.youku.java.navi.utils.NaviUtil;
import com.youku.java.navi.engine.core.INaviHttp;
import com.youku.java.navi.utils.AsynchExecUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Slf4j
public class NaviHttpClientService extends AbstractNaviDataService implements
    INaviHttp {

    public String doGet(String uri) throws NaviSystemException {
        HttpGet httpGet = new HttpGet(uri);
        try {
            return getHttpClientDrive().execute(httpGet);
        } catch (ClientProtocolException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (IOException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    public String doPost(String uri) throws NaviSystemException {
        try {
            HttpPost httPost = new HttpPost(uri);
            return getHttpClientDrive().execute(httPost);
        } catch (UnsupportedEncodingException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (ClientProtocolException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (IOException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }

    }

    public String doPost(String uri, List<BasicNameValuePair> params)
        throws NaviSystemException {
        try {
            HttpPost httPost = new HttpPost(uri);
            httPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            return getHttpClientDrive().execute(httPost);
        } catch (UnsupportedEncodingException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (ClientProtocolException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (IOException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }

    }

    public String execute(HttpUriRequest request)
        throws NaviSystemException {
        try {
            return getHttpClientDrive().execute(request);
        } catch (UnsupportedEncodingException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (ClientProtocolException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (IOException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }

    }

    public String doPut(String uri, List<BasicNameValuePair> params)
        throws NaviSystemException {
        return null;
    }

    private NaviHttpClientDriver getHttpClientDrive() {
        return (NaviHttpClientDriver) dataSource.getHandle();
    }

    public String doGet(final String uri, boolean asynchronous)
        throws NaviSystemException {
        if (asynchronous) {
            AsynchExecUtil.execute(new Runnable() {

                public void run() {
                    try {
                        String rs = doGet(uri);
                        log.debug(rs);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });
            return null;
        }
        return doGet(uri);
    }

    public String doPost(final String uri,
                         final List<BasicNameValuePair> params, boolean asynchronous)
        throws NaviSystemException {
        if (asynchronous) {
            AsynchExecUtil.execute(new Runnable() {

                public void run() {
                    try {
                        String rs = doPost(uri, params);
                        log.debug(rs);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });
            return null;
        }
        return doPost(uri, params);
    }

    public String doPut(String uri, List<BasicNameValuePair> params,
                        boolean asynchronous) throws NaviSystemException {
        return null;
    }
}
