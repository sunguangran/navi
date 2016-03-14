package com.youku.java.navi.server.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.youku.java.navi.boot.NaviDefine;
import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.server.ServerConfigure;
import com.youku.java.navi.utils.NaviUtil;

import java.util.HashMap;
import java.util.Map;

public class NaviMutiJsonResponseData extends NaviJsonResponseData {

    private Map<String, Object> filterMap;
    private String dataKey;

    public void setCost(long cost) {
        putData("cost", cost * 1.00f / 1000f);
    }

    public NaviMutiJsonResponseData(Object data) {
        super(data);
        //this.dataKey = "data";//注释by tianjianfeng
        filterMap = new HashMap<String, Object>();
    }

    public NaviMutiJsonResponseData(String dataKey, Object data) {
        super(data);
        this.dataKey = dataKey;
        filterMap = new HashMap<String, Object>();
    }

    public NaviMutiJsonResponseData(String dataKey, Object data, long total, int page, int page_length) {
        super(data);
        putData("total", total);
        putData("page", page);
        putData("page_length", page_length);
        this.dataKey = dataKey;
    }

    public void putData(String key, Object value) {
        if (filterMap == null) {
            filterMap = new HashMap<String, Object>();
        }
        filterMap.put(key, value);
    }

    @Override
    public String toResponseNull() throws NaviSystemException {
        try {
            return toJsonData("", "navi", "no data", NaviError.ERR_NO_DATA);
        } catch (JSONException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    protected String toJsonData(Object data, String provider, String desc, int code) throws JSONException {
        provider = ServerConfigure.get(NaviDefine.SERVER);
        if (null == provider || "".equals(provider)) {
            provider = "navi-server";
        }
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
        if (dataKey != null)
            putData(dataKey, data);

        JSONObject json = new JSONObject();
        if (filterMap != null) {
            for (String key : filterMap.keySet()) {
                json.put(key, filterMap.get(key));
            }
        }

        JSONObject e = new JSONObject(true);
        e.put("code", code);
        e.put("desc", desc);
        e.put("provider", provider);

        json.put("e", e);


        json.put("e", e);

        return json.toString();
    }

}
