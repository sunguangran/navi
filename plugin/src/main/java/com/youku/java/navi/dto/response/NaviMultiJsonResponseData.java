package com.youku.java.navi.dto.response;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.server.api.NaviJsonResponseData;
import com.youku.java.navi.utils.NaviUtil;

import java.util.HashMap;
import java.util.Map;

public class NaviMultiJsonResponseData extends NaviJsonResponseData {

    private int code;
    private String desc;

    private Map<String, Object> filterMap;
    private String dataKey;
    private String provider = "cloudservice";

    public NaviMultiJsonResponseData(int code, String desc) {
        super(null);
        this.code = code;
        this.desc = desc;
    }

    public NaviMultiJsonResponseData(int code, String desc, String dataKey, Object data, Integer total) {
        super(data);
        this.code = code;
        this.desc = desc;
        if (total != null) {
            putData("total", total);
        }
        this.dataKey = dataKey;
    }

    public NaviMultiJsonResponseData setProvider(String provider){
        this.provider = provider;
        return this;
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
            return toJsonData("", provider, "no data found", NaviError.ERR_NO_DATA);
        } catch (JSONException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    @Override
    protected String toJsonData(Object data1, String provider, String desc, int code) throws JSONException {
        provider = this.provider;
        if (this.data instanceof JSONArray) {
            JSONArray array = (JSONArray) this.data;
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i) instanceof JSONObject) {
                    JSONObject json = array.getJSONObject(i);
                    if (json.containsKey("_id")) {
                        json.put("id", json.getLong("_id"));
                        json.remove("_id");
                    }
                }
            }
        } else if (this.data instanceof JSONObject) {
            JSONObject json = (JSONObject) this.data;
            if (json.containsKey("_id")) {
                json.put("id", json.getLong("_id"));
                json.remove("_id");
            }
        }

        if (dataKey != null) {
            putData(dataKey, this.data);
        }

        putData("cost", cost * 0.001f);

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
