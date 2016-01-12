package com.cuckoo.framework.navi.server.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cuckoo.framework.navi.common.exception.NaviBusinessException;
import com.cuckoo.framework.navi.common.exception.NaviSystemException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

import java.util.Collection;

@Slf4j
@Setter
@Getter
public abstract class ANaviResponseData implements INaviResponseData {

    protected String dataKey;
    protected Object data;
    protected int page;
    protected int count;
    protected long cost;
    protected long total;

    public ANaviResponseData(Object data) {
        this.data = data;
    }

    public ANaviResponseData(Object data, String dataKey, long total, int page, int count) {
        this.data = data;
        this.page = page;
        this.total = total;
        this.count = count;
        this.dataKey = dataKey;
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
