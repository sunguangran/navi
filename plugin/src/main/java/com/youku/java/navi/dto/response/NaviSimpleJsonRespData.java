package com.youku.java.navi.dto.response;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.server.api.NaviJsonResponseData;
import com.youku.java.navi.utils.NaviUtil;

import java.util.HashMap;
import java.util.Map;

public class NaviSimpleJsonRespData extends NaviJsonResponseData {

    private Map<String, Object> filterMap;
    private String dataKey;

    private int code;
    private String desc;
    private String provider = "cloudservice";

    public void setCost(long cost) {
        putData("cost", cost * 1.00f / 1000f);
    }


    public NaviSimpleJsonRespData setProvider(String provider){
        this.provider = provider;
        return this;
    }

    public NaviSimpleJsonRespData(int code, String desc) {
        super(null);
        this.code = code;
        this.desc = desc;
    }

    public static NaviSimpleJsonRespData createInstance(int code) {
        return NaviSimpleJsonRespData.createInstance(code, "");
    }

    public static NaviSimpleJsonRespData createInstance(int code, String desc) {
        return new NaviSimpleJsonRespData(code, desc);
    }

    public void putData(String key, Object value) {
        if (filterMap == null) {
            filterMap = new HashMap<>();
        }
        filterMap.put(key, value);
    }

    @Override
    public String toResponseNull() throws NaviSystemException {
        try {
            return toJsonData("", provider, this.desc, this.code);
        } catch (JSONException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    @Override
    protected String toJsonData(Object data, String provider, String desc, int code) throws JSONException {
        provider = this.provider;
        if (data instanceof JSONArray) {
            JSONArray array = (JSONArray) data;
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i) instanceof JSONObject) {
                    JSONObject json = array.getJSONObject(i);
                    if (json.containsKey("_id")) {
                        json.put("id", json.getLong("_id"));
                        json.remove("_id");
                    }
                }
            }
        } else if (data instanceof JSONObject) {
            JSONObject json = (JSONObject) data;
            if (json.containsKey("_id")) {
                json.put("id", json.getLong("_id"));
                json.remove("_id");
            }
        }

        if (dataKey != null) {
            putData(dataKey, data);
        }

        JSONObject e = new JSONObject(true);
        e.put("code", code);
        e.put("desc", desc);
        e.put("provider", provider);

        JSONObject json = new JSONObject(true);
        json.put("e", e);

        if (filterMap != null) {
            for (String key : filterMap.keySet()) {
                json.put(key, filterMap.get(key));
            }
        }

        return json.toString();
    }

}
