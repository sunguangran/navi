package com.cuckoo.framework.navi.common.exception;

import com.alibaba.fastjson.JSONObject;
import com.cuckoo.framework.navi.server.api.INaviResponseData;
import lombok.Getter;
import lombok.Setter;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

@Setter
@Getter
public class NaviBusiException extends NaviRuntimeException implements INaviResponseData {

    private static final long serialVersionUID = -1118018339885279706L;

    private String provider;
    private long cost;
    private int page;
    private int count;
    private Object data;

    public NaviBusiException(String desc, int code) {
        this(null, desc, code);
    }

    public NaviBusiException(String provider, String desc, int code) {
        super(code, desc);
        this.provider = provider;
    }

    public ChannelBuffer getResponseData() throws Exception {
        return ChannelBuffers.copiedBuffer(toJsonString(), CharsetUtil.UTF_8);
    }

    public String getResponseType() {
        return "text/plain;charset=UTF-8";
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
}
