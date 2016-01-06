package com.cuckoo.framework.navi.api;

import com.cuckoo.framework.navi.boot.NaviProps;
import com.cuckoo.framework.navi.common.NAVIERROR;
import com.cuckoo.framework.navi.common.NaviSystemException;
import com.cuckoo.framework.navi.server.ServerConfigure;
import com.cuckoo.framework.navi.utils.NaviUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
            return toJsonData("", "navi", "no data",
                NAVIERROR.BUSI_NO_DATA.code());
        } catch (JSONException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    protected String toJsonData(Object data, String provider, String desc, int code) throws JSONException {
        provider = ServerConfigure.get(NaviProps.SERVER);
        if (null == provider || "".equals(provider)) {
            provider = "navi-server";
        }
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
        if (dataKey != null)
            putData(dataKey, data);

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
