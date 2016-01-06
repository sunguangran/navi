package com.cuckoo.framework.navi.test;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestOKTestCase extends NaviAPITestCase {

    public TestOKTestCase() {
        super("http://10.10.69.151:8089", "passport", "passport", "get_by_id");
    }


    public void testId() throws ClientProtocolException, IOException, JSONException {
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("ids", "123"));
        JSONObject rsJson = sendRequest(params);
        assertNotNull(rsJson.getJSONArray("data").getJSONObject(0).getString("username"));
    }
}
