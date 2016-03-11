package com.youku.java.navi.server.api;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;

public class NaviHisJsonResponseData extends NaviJsonResponseData {

    public NaviHisJsonResponseData(Object data) {
        super(data);
    }

    @Override
    protected String toJsonData(Object data, String provider, String desc, int code) throws JSONException {
        JSONObject ret = new JSONObject();
        ret.put("errno", code);
        ret.put("errText", StringUtils.isEmpty(desc) ? "OK" : desc);
        ret.put("data", data);

        return ret.toString();
    }
}
