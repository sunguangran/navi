package com.youku.java.navi.common.exception;

import com.alibaba.fastjson.JSONObject;
import com.youku.java.navi.server.api.INaviResponseData;
import lombok.Getter;
import lombok.Setter;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

@Setter
@Getter
public class NaviBusinessException extends NaviRuntimeException implements INaviResponseData {

    private static final long serialVersionUID = 3535592633572343507L;

    private String provider;
    private long cost;

    public NaviBusinessException(String desc, int code) {
        this(null, desc, code);
    }

    public NaviBusinessException(String provider, String desc, int code) {
        super(desc, code);
        this.provider = provider;
    }

    public ChannelBuffer getResponseData() throws Exception {
        return ChannelBuffers.copiedBuffer(toJsonString(), CharsetUtil.UTF_8);
    }

    public String getResponseType() {
        return "text/plain;charset=UTF-8";
    }

    public void setData(Object data) {
    }

    protected String toJsonString() {
        JSONObject e = new JSONObject();
        e.put("provider", provider);
        e.put("desc", toString());
        e.put("code", getCode());

        JSONObject ret = new JSONObject();
        ret.put("data", "");
        ret.put("cost", cost * 0.001f);
        ret.put("e", e);

        return ret.toJSONString();
    }

    public void setPage(int page) {
    }

    public void setPageLength(int pageLength) {
    }
}
