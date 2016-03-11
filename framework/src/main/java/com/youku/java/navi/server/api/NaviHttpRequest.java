package com.youku.java.navi.server.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.youku.java.navi.utils.NaviUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * navi框架默认request对象，为DefaultHttpRequest对象适配器
 */
@Setter
@Getter
@Slf4j
public class NaviHttpRequest extends DefaultHttpRequest {

    private HttpRequest request;
    private String server;
    private String moduleNm;
    private String uri;
    private JSONObject params = new JSONObject();
    private Map<String, Object> datas = new HashMap<>();

    public NaviHttpRequest(HttpRequest request) {
        super(request.getProtocolVersion(), request.getMethod(), request.getUri());
        this.request = request;
        parseParameter();
    }

    private void parseParameter() {
        QueryStringDecoder decoder;
        if (getMethod().equals(HttpMethod.GET)) {
            decoder = new QueryStringDecoder(getUri(), Charset.forName("UTF-8"));
            traversalDecoder(decoder);
        } else {
            //获取URI中的参数
            if (getUri() != null) {
                decoder = new QueryStringDecoder(getUri(), Charset.forName("UTF-8"));
                traversalDecoder(decoder);
            }

            //获取postData中的参数
            String content = request.getContent().toString(Charset.forName("UTF-8"));
            try {
                // content为json参数格式
                this.params = JSON.parseObject(content);
            } catch (JSONException e) {
                // content为非json参数格式
                decoder = new QueryStringDecoder("/?" + content + "&");
                traversalDecoder(decoder);
            }
        }

        if (params == null) {
            params = new JSONObject();
        }
    }

    private void traversalDecoder(QueryStringDecoder decoder) {
        try {
            for (Map.Entry<String, List<String>> entry : decoder.getParameters().entrySet()) {
                params.put(entry.getKey(), entry.getValue().get(0));
            }
        } catch (JSONException e) {
            throw NaviUtil.transferToNaviSysException(e);
        }
    }

    public String getParameter(String name) {
        return params.getString(name);
    }

    public JSONObject getJsonParamter(String name) {
        return params.getJSONObject(name);
    }

    public JSONArray getJsonArrayParamter(String name) {
        JSONArray array = params.getJSONArray(name);
        if (array == null) {
            try {
                return new JSONArray();
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }
        return array;
    }

    public boolean isEmpty(String name) {
        String value = getParameter(name);
        return StringUtils.isEmpty(value);
    }

    public Object addObject(String name, Object obj) {
        return datas.put(name, obj);
    }

    public Object getObject(String name) {
        return datas.get(name);
    }

    @Override
    public void addHeader(String name, Object value) {
        request.headers().add(name, value);
    }

    @Override
    public void setHeader(String name, Iterable<?> values) {
        request.headers().set(name, values);
    }

    @Override
    public void removeHeader(final String name) {
        request.headers().remove(name);
    }

    @Override
    public void clearHeaders() {
        request.headers().clear();
    }

    @Override
    public List<String> getHeaders(final String name) {
        return request.headers().getAll(name);
    }

    @Override
    public List<Map.Entry<String, String>> getHeaders() {
        return request.headers().entries();
    }

    @Override
    public boolean containsHeader(String name) {
        return request.headers().contains(name);
    }

    @Override
    public Set<String> getHeaderNames() {
        return request.headers().names();
    }

    @Override
    public ChannelBuffer getContent() {
        return request.getContent();
    }

    public String getClientIP() {
        return request.headers().get(Names.HOST);
    }

    @Override
    public void setContent(ChannelBuffer content) {
        request.setContent(content);
    }

}
