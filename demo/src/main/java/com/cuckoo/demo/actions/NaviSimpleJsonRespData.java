package com.cuckoo.demo.actions;

import com.cuckoo.framework.navi.api.NaviJsonResponseData;
import com.cuckoo.framework.navi.common.NaviSystemException;
import com.cuckoo.framework.navi.utils.NaviUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NaviSimpleJsonRespData extends NaviJsonResponseData {

    private Map<String, Object> filterMap;

    private int code;
    private String desc;

    public void setCost(long cost) {
        putData("cost", cost * 1.00f / 1000f);
    }

    public NaviSimpleJsonRespData(int code, String desc) {
        super(null);
        this.code = code;
        this.desc = desc;
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
            return toJsonData("", "mfp", this.desc, this.code);
        } catch (JSONException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    @Override
    protected String toJsonData(Object data, String provider, String desc, int code) throws JSONException {
        provider = "mfp";
        if (data instanceof JSONArray) {
            JSONArray array = (JSONArray) data;
            for (int i = 0; i < array.length(); i++) {
                if (array.get(i) instanceof JSONObject) {
                    JSONObject json = array.getJSONObject(i);
                    if (json.has("_id")) {
                        json.put("id", json.getLong("_id"));
                        json.remove("_id");
                    }
                }
            }
        } else if (data instanceof JSONObject) {
            JSONObject json = (JSONObject) data;
            if (json.has("_id")) {
                json.put("id", json.getLong("_id"));
                json.remove("_id");
            }
        }

        JSONObject json = new JSONObject();
        if (filterMap != null) {
            for (String key : filterMap.keySet()) {
                json.put(key, filterMap.get(key));
            }
        }
        json.put("e", new JSONObject().put("provider", provider).put("desc", desc).put("code", code));

        return json.toString();
    }

}
