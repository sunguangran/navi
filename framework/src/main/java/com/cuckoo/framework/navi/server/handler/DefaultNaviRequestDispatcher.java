package com.cuckoo.framework.navi.server.handler;

import com.cuckoo.framework.navi.api.ANaviAction;
import com.cuckoo.framework.navi.api.NaviHttpRequest;
import com.cuckoo.framework.navi.api.NaviHttpResponse;
import com.cuckoo.framework.navi.boot.NaviDefine;
import com.cuckoo.framework.navi.common.NAVIERROR;
import com.cuckoo.framework.navi.common.NaviSystemException;
import com.cuckoo.framework.navi.common.RestApi;
import com.cuckoo.framework.navi.module.INaviModuleContext;
import com.cuckoo.framework.navi.module.NaviModuleContextFactory;
import com.cuckoo.framework.navi.server.ServerConfigure;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.util.*;

public class DefaultNaviRequestDispatcher extends AbstractNaviRequestDispatcher {
    private List<String> redirectList;
    private Map<String, String> redirectMap;

    @Override
    public NaviHttpRequest packageNaviHttpRequest(HttpRequest request) throws Exception {
        String uri = request.getUri();
        //重定向
        uri = redirect(uri);
        if (uri == null || uri.length() == 0) {
            throw new NaviSystemException("malformed URL!", NAVIERROR.SYSERROR.code());
        }

        if (uri.indexOf('?') > 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }

        String[] uriSplits = uri.split("/");
        if (uriSplits.length < 4) {
            throw new NaviSystemException("malformed URL!", NAVIERROR.SYSERROR.code());
        } else if (!uriSplits[1].equals(ServerConfigure.get(NaviDefine.SERVER))) {
            throw new NaviSystemException("invalid server name: " + uriSplits[1] + ".", NAVIERROR.SYSERROR.code());
        }

        NaviHttpRequest naviReq = new NaviHttpRequest(request);
        naviReq.setServer(uriSplits[1]);
        naviReq.setModuleNm(uriSplits[2]);
        naviReq.setUri(uri);
        return naviReq;
    }

    @Override
    public void callApi(NaviHttpRequest request, NaviHttpResponse response) throws Exception {
        INaviModuleContext moduleCtx = NaviModuleContextFactory.getInstance().getNaviModuleContext(request.getModuleNm());
        if (moduleCtx == null) {
            throw new NaviSystemException("module " + request.getModuleNm() + " not found!", NAVIERROR.SYSERROR.code());
        }

        RestApi restApi = NaviModuleContextFactory.getInstance().getRestApi(request.getUri());
        if (restApi == null) {
            throw new NaviSystemException("'" + request.getUri() + "' not found!", NAVIERROR.SYSERROR.code());
        }

        ANaviAction bean = (ANaviAction) moduleCtx.getBean(NaviModuleContextFactory.getInstance().getBeanId(restApi.getModuleNm(), restApi.getClazz()));
        if (bean == null) {
            throw new NaviSystemException("'" + request.getUri() + "' not found!", NAVIERROR.SYSERROR.code());
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
