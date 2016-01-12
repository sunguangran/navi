package com.cuckoo.framework.navi.server.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class NaviComplexJsonResponseData extends NaviJsonResponseData {

    private Map<String, Object> filterMap;

    public NaviComplexJsonResponseData(Object data) {
        this("data", data);
    }

    public NaviComplexJsonResponseData(String dataKey, Object data) {
        this(dataKey, data, -1, -1, -1);
    }

    public NaviComplexJsonResponseData(String dataKey, Object data, int page, int count, long total) {
        super(data);
        filterMap = new HashMap<>();

        if (page > 0) {
            putData("page", page);
        }

        if (count >= 0) {
            putData("count", count);
        }

        if (total >= 0) {
            putData("total", total);
        }

        this.dataKey = dataKey;
    }

    public Map<String, Object> putData(String key, Object value) {
        if (filterMap == null) {
            filterMap = new HashMap<>();
        }

        filterMap.put(key, value);
        return filterMap;
    }

    @Override
    protected String toJsonData(int code, String desc, Object data) throws JSONException {
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
                json.put("id", json.remove("_id"));
            }
        }

        if (dataKey != null) {
            putData(dataKey, data);
        }

        JSONObject json = new JSONObject(true);

        // e node
        JSONObject e = new JSONObject(true);
        e.put("code", code);
        e.put("desc", desc);
        e.put("provider", provider);

        json.put("e", e);
        json.put("cost", cost * 0.001F);

        if (filterMap != null) {
            for (String key : filterMap.keySet()) {
                json.put(key, filterMap.get(key));
            }
        }

        return json.toJSONString();
    }
}
