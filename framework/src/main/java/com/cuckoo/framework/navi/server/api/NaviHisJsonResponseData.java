package com.cuckoo.framework.navi.server.api;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;

public class NaviHisJsonResponseData extends NaviJsonResponseData {

    public NaviHisJsonResponseData(Object data) {
        super(data);
    }

    @Override
    protected String toJsonData(Object data, String provider, String desc, int code) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("errno", code);
        json.put("errText", StringUtils.isEmpty(desc) ? "OK" : desc);
        json.put("data", data);

        return json.toJSONString();
    }
}
