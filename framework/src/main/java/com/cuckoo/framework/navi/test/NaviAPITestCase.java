package com.cuckoo.framework.navi.test;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import junit.framework.TestCase;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Navi API测试用例基类
 */
public class NaviAPITestCase extends TestCase {

    private String url;
    private HttpClient httpClient;
    private BasicResponseHandler responseHandler;

    public NaviAPITestCase(String uri, String module, String api) {
        this(uri, "navi-server", module, api);
    }

    public NaviAPITestCase(String uri, String server, String module, String api) {
        this.url = new StringBuilder().append(uri).append("/").append(server)
            .append("/").append(module).append("/").append(api).toString();
    }

    public JSONObject sendRequest(List<BasicNameValuePair> params) throws IOException, JSONException {
        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        String rs = getHttpClient().execute(post, getResponseHandler());
        return JSONObject.parseObject(rs);
    }

    protected BasicResponseHandler getResponseHandler() {
        if (responseHandler == null) {
            responseHandler = new BasicResponseHandler();
        }
        return responseHandler;
    }

    protected HttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new DefaultHttpClient(
                new PoolingClientConnectionManager(SchemeRegistryFactory.createDefault(), 3000, TimeUnit.MILLISECONDS)
            );
        }
        return httpClient;
    }
}
