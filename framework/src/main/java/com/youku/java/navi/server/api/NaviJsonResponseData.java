package com.youku.java.navi.server.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.exception.NaviBusinessException;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.server.ServerConfigure;
import com.youku.java.navi.server.serviceobj.AbstractNaviDto;
import com.youku.java.navi.utils.NaviUtil;
import com.youku.java.navi.utils.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class NaviJsonResponseData extends ANaviResponseData {

    public NaviJsonResponseData(int code, String desc) {
        super(code, desc);
    }

    public NaviJsonResponseData(Object data) {
        this(data, "data", 0, 0, 0);
    }

    public NaviJsonResponseData(Object data, int count) {
        this(data, "data", 0, count, 0);
    }

    public NaviJsonResponseData(Object data, int page, int count, long total) {
        this(data, "data", page, count, total);
    }

    public NaviJsonResponseData(Object data, String dataFieldNm, int page, int count, long total) {
        super(data, dataFieldNm, page, count, total);
    }

    @Override
    protected String toJsonData(Object data, String provider, String desc, int code) throws JSONException {
        JSONObject re = new JSONObject(true);

        JSONObject e = new JSONObject(true);
        e.put("code", code);
        e.put("desc", StringUtils.isNotEmpty(desc) ? desc : "");
        e.put("provider", StringUtils.isEmpty(provider) ? ServerConfigure.getServer() : provider);

        re.put("e", e);

        if (page > 0) {
            re.put("page", page);
        }
        if (count > 0) {
            re.put("count", count);
        }
        if (total > 0) {
            re.put("total", total);
        }

        if (filterMap != null) {
            for (String key : filterMap.keySet()) {
                re.put(key, filterMap.get(key));
            }
        }

        re.put("cost", cost * 0.001F);

        if (data != null) {
            re.put(dataFieldNm, data);
        }

        return re.toString();
    }

    public String getResponseType() {
        return "text/plain;charset=UTF-8";
    }

    @Override
    public String toResponseNull() throws NaviSystemException {
        try {
            return toJsonData(null, "", "no data found", NaviError.ERR_NO_DATA);
        } catch (JSONException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    @Override
    public String toResponseForBusinessException() throws NaviSystemException {
        try {
            NaviBusinessException ex = (NaviBusinessException) data;
            return toJsonData(null, ex.getProvider(), ex.toString(), ex.getCode());
        } catch (JSONException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    @Override
    public String toResponseForList() throws NaviSystemException {
        try {
            JSONArray datas = new JSONArray();

            Collection list = (Collection) data;
            if (list.size() == 0) {
                return toResponseNull();
            }

            for (Object data : list) {
                if (data instanceof AbstractNaviDto) {
                    datas.add(NaviUtil.toJSONObject((AbstractNaviDto) data));
                }
            }

            return toJsonData(datas, "", "", 0);
        } catch (JSONException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    @Override
    public String toResponseForArray() throws NaviSystemException {
        if (data == null || !(data instanceof AbstractNaviDto[])) {
            return toResponseNull();
        }

        try {
            AbstractNaviDto[] dtos = (AbstractNaviDto[]) data;
            JSONArray datas = new JSONArray();
            for (AbstractNaviDto dto : dtos) {
                datas.add(NaviUtil.toJSONObject(dto));
            }
            return datas.toString();
        } catch (SecurityException | IllegalArgumentException | JSONException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    @Override
    public String toResponseForObject() throws NaviSystemException {
        try {
            return toJsonData(
                (data instanceof AbstractNaviDto) ? NaviUtil.toJSONObject((AbstractNaviDto) data) : data.toString(), "", "", 0);
        } catch (SecurityException | IllegalArgumentException | NoSuchMethodException | IllegalAccessException | JSONException | InvocationTargetException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    @Override
    protected String toResponseForJsonArray() {
        try {
            return toJsonData(data, "", "", 0);
        } catch (JSONException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    @Override
    protected String toResponseForJsonObject() throws NaviSystemException {
        try {
            return toJsonData(data, "", "", 0);
        } catch (JSONException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }
}
