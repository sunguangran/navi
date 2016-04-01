package com.youku.java.navi.server.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.exception.NaviBusinessException;
import com.youku.java.navi.common.exception.NaviSystemException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Setter
public abstract class ANaviResponseData implements INaviResponseData {

    protected int code = NaviError.ACTION_SUCCED;
    protected String desc = "";
    protected String provider = "cloudservice";

    protected int page;
    protected int count;
    protected long cost;
    protected long total;

    protected String dataFieldNm = "data";
    protected Object data;

    protected Map<String, Object> filterMap;

    public ANaviResponseData(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public ANaviResponseData(Object data) {
        this.data = data;
    }

    public ANaviResponseData(Object data, String dataFieldNm, int page, int count, long total) {
        this.data = data;
        this.page = page;
        this.total = total;
        this.count = count;
        this.dataFieldNm = dataFieldNm;
    }

    public void putData(String key, Object value) {
        if (filterMap == null) {
            filterMap = new HashMap<>();
        }

        filterMap.put(key, value);
    }

    public ChannelBuffer getResponseData() throws Exception {
        return ChannelBuffers.copiedBuffer(toResponse(), CharsetUtil.UTF_8);
    }

    protected String toResponse() throws NaviSystemException {
        if (data != null) {
            if (data == null) {
                return toResponseNull();
            } else if (data instanceof NaviBusinessException) {
                return toResponseForBusinessException();
            } else if (data instanceof JSONArray) {
                return toResponseForJsonArray();
            } else if (data instanceof JSONObject) {
                return toResponseForJsonObject();
            } else if (data instanceof Collection) {
                return toResponseForList();
            } else if (data.getClass().isArray()) {
                return toResponseForArray();
            }
            return toResponseForObject();
        }

        return  toJsonData(null, "", desc, code);
    }

    protected abstract String toJsonData(Object data, String provider, String desc, int code) throws JSONException;

    protected abstract String toResponseForJsonObject() throws NaviSystemException;

    protected abstract String toResponseForJsonArray() throws NaviSystemException;

    @Override
    public String toString() {
        try {
            return toResponse();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 处理空数据返回
     *
     * @return
     */
    public abstract String toResponseNull() throws NaviSystemException;

    /**
     * 处理业务异常返回
     *
     * @return
     */
    public abstract String toResponseForBusinessException()
        throws NaviSystemException;

    /**
     * 处理动态数组返回
     *
     * @return
     */
    public abstract String toResponseForList() throws NaviSystemException;

    /**
     * 处理静态数组返回
     *
     * @return
     */
    public abstract String toResponseForArray() throws NaviSystemException;

    /**
     * 处理单一对象返回
     *
     * @return
     */
    public abstract String toResponseForObject() throws NaviSystemException;

}
