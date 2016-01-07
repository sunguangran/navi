package com.cuckoo.framework.navi.server.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.cuckoo.framework.navi.common.NaviError;
import com.cuckoo.framework.navi.common.exception.NaviBusinessException;
import com.cuckoo.framework.navi.common.exception.NaviSystemException;
import com.cuckoo.framework.navi.server.ServerConfigure;
import com.cuckoo.framework.navi.serviceobj.AbstractNaviDto;
import com.cuckoo.framework.navi.utils.NaviUtil;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class NaviJsonResponseData extends ANaviResponseData {

    public NaviJsonResponseData(Object data) {
        this(data, "data", 0, 0, 0);
    }

    public NaviJsonResponseData(Object data, String dataFieldNm, long total, int page, int pageLength) {
        super(data, dataFieldNm, total, page, pageLength);
    }

    protected String toJsonData(Object data, String provider, String desc, int code) throws JSONException {
        JSONObject re = new JSONObject();
        re.put(dataKey, data != null ? data : "");
        if (page > 0) {
            re.put("page", page);
        }
        if (count > 0) {
            re.put("count", count);
        }
        if (total > 0) {
            re.put("total", total);
        }
        re.put("cost", cost * 0.001f);
        JSONObject e = new JSONObject();
        e.put("provider", ServerConfigure.getServer());
        e.put("desc", StringUtils.isNotEmpty(desc) ? desc : "");
        e.put("code", code);
        re.put("e", e);

        return re.toString();
    }

    public String getResponseType() {
        return "text/plain;charset=UTF-8";
    }

    @Override
    public String toResponseNull() throws NaviSystemException {
        try {
            return toJsonData("", "", "\"no data!\"", NaviError.BUSI_NO_DATA.code());
        } catch (JSONException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    @Override
    public String toResponseForBusinessException() throws NaviSystemException {
        try {
            NaviBusinessException ex = (NaviBusinessException) data;
            return toJsonData("", ex.getProvider(), ex.toString(), ex.getCode());
        } catch (JSONException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    @Override
    public String toResponseForList() throws NaviSystemException {
        try {
            JSONArray datas = new JSONArray();
            @SuppressWarnings("rawtypes")
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
        } catch (JSONException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (SecurityException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (IllegalArgumentException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (NoSuchMethodException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (IllegalAccessException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (InvocationTargetException e) {
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
        } catch (SecurityException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (IllegalArgumentException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (JSONException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (NoSuchMethodException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (IllegalAccessException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (InvocationTargetException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    @Override
    public String toResponseForObject() throws NaviSystemException {
        try {
            return toJsonData(
                (data instanceof AbstractNaviDto) ? NaviUtil.toJSONObject((AbstractNaviDto) data) : data.toString(), "", "", 0);
        } catch (SecurityException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (IllegalArgumentException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (JSONException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (NoSuchMethodException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (IllegalAccessException e) {
            throw NaviUtil.transferToNaviSysException(e);
        } catch (InvocationTargetException e) {
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
