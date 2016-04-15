package com.youku.java.navi.server.api;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Setter
@Getter
public class NaviHttpResponse extends DefaultHttpResponse {

    private DefaultHttpResponse response;
    private INaviResponseData responseData;
    private Map<String, Object> map;

    public NaviHttpResponse(DefaultHttpResponse response) {
        super(response.getProtocolVersion(), response.getStatus());
        this.response = response;
        map = new HashMap<>();
    }

    public NaviHttpResponse(HttpVersion version, HttpResponseStatus status) {
        super(version, status);
        map = new HashMap<>();
    }

    public void setResponseData(INaviResponseData data) {
        this.responseData = data;
    }

    public void setResponseData(int code, String desc) {
        this.setResponseData(new NaviJsonResponseData(code, desc));
    }

    public void setResponseData(JSON data) {
        this.setResponseData(new NaviJsonResponseData(data));
    }

    @Override
    public void addHeader(String name, Object value) {
        if (response != null) {
            response.headers().add(name, value);
        } else {
            super.headers().add(name, value);
        }
    }

    @Override
    public void setHeader(String name, Iterable<?> values) {
        if (response != null) {
            response.headers().set(name, values);
        } else {
            super.headers().set(name, values);
        }
    }

    @Override
    public void removeHeader(final String name) {
        if (response != null) {
            response.headers().remove(name);
        } else {
            super.headers().remove(name);
        }
    }

    @Override
    public void clearHeaders() {
        if (response != null) {
            response.headers().clear();
        } else {
            super.headers().clear();
        }
    }

    @Override
    public List<String> getHeaders(final String name) {
        if (response != null) {
            return response.headers().getAll(name);
        } else {
            return super.headers().getAll(name);
        }
    }

    @Override
    public List<Map.Entry<String, String>> getHeaders() {
        if (response != null) {
            return response.headers().entries();
        } else {
            return super.headers().entries();
        }
    }

    @Override
    public boolean containsHeader(final String name) {
        if (response != null) {
            return response.headers().contains(name);
        } else {
            return super.headers().contains(name);
        }
    }

    @Override
    public Set<String> getHeaderNames() {
        if (response != null) {
            return response.headers().names();
        } else {
            return super.headers().names();
        }
    }

    @Override
    public void setContent(ChannelBuffer content) {
        if (response != null) {
            response.setContent(content);
        } else {
            super.setContent(content);
        }
    }

    @Override
    public ChannelBuffer getContent() {
        if (response != null) {
            return response.getContent();
        } else {
            return super.getContent();
        }
    }

    public void setJsonData(Object data) {
        if (responseData != null && responseData instanceof NaviJsonResponseData) {
            responseData.setData(data);
        } else {
            responseData = new NaviJsonResponseData(data);
        }
    }

    public void setJsonData(Object data, String dataFieldNm, int page, int count, long total) {
        if (responseData != null && responseData instanceof NaviJsonResponseData) {
            responseData.setData(data);
        } else {
            responseData = new NaviJsonResponseData(data, dataFieldNm, page, count, total);
        }
    }

    public void putObject(String name, Object value) {
        map.put(name, value);
    }

    public Object getObject(String name) {
        return map.get(name);
    }

}
