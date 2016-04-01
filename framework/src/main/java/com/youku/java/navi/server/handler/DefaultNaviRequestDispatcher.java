package com.youku.java.navi.server.handler;

import com.youku.java.navi.boot.NaviDefine;
import com.youku.java.navi.common.NaviError;
import com.youku.java.navi.common.RestApi;
import com.youku.java.navi.common.exception.NaviBusinessException;
import com.youku.java.navi.common.exception.NaviSystemException;
import com.youku.java.navi.server.ServerConfigure;
import com.youku.java.navi.server.api.ANaviAction;
import com.youku.java.navi.server.api.NaviHttpRequest;
import com.youku.java.navi.server.api.NaviHttpResponse;
import com.youku.java.navi.server.module.INaviModuleContext;
import com.youku.java.navi.server.module.NaviModuleContextFactory;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.util.*;

public class DefaultNaviRequestDispatcher extends AbstractNaviRequestDispatcher {
    private List<String> redirectList;
    private Map<String, String> redirectMap;

    @Override
    public NaviHttpRequest packageNaviHttpRequest(HttpRequest request) throws Exception {
        String uri = request.getUri();

        // 过滤　"//"
        uri = uri.replaceAll("/(/)+", "/");

        // 重定向
        uri = redirect(uri);
        if (uri == null || uri.length() == 0) {
            throw new NaviSystemException("malformed URL!", NaviError.SYSERROR);
        }

        if (uri.indexOf('?') > 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }

        String[] uriSplits = uri.split("/");
        if (uriSplits.length < 2) {
            throw new NaviSystemException("malformed URL!", NaviError.SYSERROR);
        }

        NaviHttpRequest naviReq = new NaviHttpRequest(request);
        naviReq.setServer(ServerConfigure.get(NaviDefine.SERVER));
        naviReq.setModuleNm(uriSplits[1]);
        naviReq.setUri(uri);
        return naviReq;
    }

    @Override
    public void callApi(NaviHttpRequest request, NaviHttpResponse response) throws Exception {
        INaviModuleContext moduleCtx = NaviModuleContextFactory.getInstance().getNaviModuleContext(request.getModuleNm());
        if (moduleCtx == null) {
            throw new NaviBusinessException("module " + request.getModuleNm() + " not found!", NaviError.ACTION_NOT_SUPPORTED);
        }

        String uri = request.getUri();
        uri = uri.substring(uri.indexOf(request.getModuleNm()) + request.getModuleNm().length());

        RestApi restApi = NaviModuleContextFactory.getInstance().getRestApi(request.getModuleNm(), uri);
        if (restApi == null) {
            throw new NaviBusinessException("'" + request.getUri() + "' not found!", NaviError.ACTION_NOT_SUPPORTED);
        }

        ANaviAction bean = (ANaviAction) moduleCtx.getBean(NaviModuleContextFactory.getInstance().getBeanId(request.getModuleNm(), restApi.getClazz()));
        if (bean == null) {
            throw new NaviBusinessException("'" + request.getUri() + "' not found!", NaviError.ACTION_NOT_SUPPORTED);
        }

        bean.action(request, response, restApi.getMethod());
    }

    public String redirect(String url) {
        if (null == redirectList) {
            redirectList = new ArrayList<>();
            redirectMap = new HashMap<>();
            String redirect = ServerConfigure.get(NaviDefine.REDIRECT_STR);
            if (null != redirect) {
                String[] elements = redirect.split(",");
                for (String ele : elements) {
                    String key = ele.split(":")[0];
                    String val = ele.split(":")[1];
                    redirectMap.put(key, val);
                }
                redirectList.addAll(redirectMap.keySet());
                Collections.sort(redirectList);
            }
        }

        if (null != redirectList && redirectList.size() > 0) {
            for (int i = redirectList.size() - 1; i >= 0; i--) {
                String key = redirectList.get(i);
                //url.contains(key.replace("\\",""))
                //url.replaceFirst(key.replace("\\\\",""), redirectMap.get(key));
                if (url.contains(key.replace("\\", ""))) {
                    return url.replaceFirst(key.replace("\\\\", ""), redirectMap.get(key));
                }
            }
        }

        return url;
    }
}
