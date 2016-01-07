package com.cuckoo.framework.navi.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cuckoo.framework.navi.common.NaviBusinessException;
import com.cuckoo.framework.navi.common.NaviSystemException;
import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

import java.util.Collection;

@Slf4j
public abstract class AbstractNaviResponseData implements INaviResponseData {

    protected Object data;
    protected int page;
    protected int pageLength;
    protected long cost;
    protected long total;
    protected String dataFieldNm;

    public AbstractNaviResponseData(Object data) {
        this.data = data;
    }

    public AbstractNaviResponseData(Object data, String dataFieldNm, long total, int page, int pageLength) {
        this.data = data;
        this.page = page;
        this.total = total;
        this.pageLength = pageLength;
        this.dataFieldNm = dataFieldNm;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setPage(int page) {
        this.page = page;
    }


    public void setPageLength(int pageLength) {
        this.pageLength = pageLength;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public ChannelBuffer getResponseData() throws Exception {
        return ChannelBuffers.copiedBuffer(toResponse(), CharsetUtil.UTF_8);
    }

    protected String toResponse() throws NaviSystemException {
        if (data == null) {
            return toResponseNull();
        } else if (data instanceof NaviBusinessException) {
            return toResponseForBusinessException();
        } else if (data instanceof Collection) {
            return toResponseForList();
        } else if (data.getClass().isArray()) {
            return toResponseForArray();
        } else if (data instanceof JSONArray) {
            return toResponseForJsonArray();
        } else if (data instanceof JSONObject) {
            return toResponseForJsonObject();
        }
        return toResponseForObject();
    }

    protected abstract String toResponseForJsonObject()
        throws NaviSystemException;

    protected abstract String toResponseForJsonArray()
        throws NaviSystemException;

    @Override
    public String toString() {
        try {
            return toResponse().toString();
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
