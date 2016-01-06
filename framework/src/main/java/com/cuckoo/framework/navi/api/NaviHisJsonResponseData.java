package com.cuckoo.framework.navi.api;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class NaviHisJsonResponseData extends NaviJsonResponseData {

    public NaviHisJsonResponseData(Object data) {
        super(data);
    }

    @Override
    protected String toJsonData(Object data, String provider, String desc, int code) throws JSONException {
        return new JSONObject()
            .put("errno", code)
            .put("errText", StringUtils.isEmpty(desc) ? "OK" : desc)
            .put("data", data)
            .toString();
    }
}
