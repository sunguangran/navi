package com.youku.java.navi.engine.async;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.youku.java.navi.engine.core.INaviMessageQueue;
import com.youku.java.navi.server.ServerConfigure;
import com.youku.java.navi.server.module.INaviModuleContext;
import com.youku.java.navi.server.module.NaviAsyncModuleContextFactory;
import com.youku.java.navi.server.module.NaviModuleContextFactory;
import com.youku.java.navi.utils.AlibabaJsonSerializer;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class NaviAsynchronousSender {

    private INaviMessageQueue queue;
    private boolean async = true;

    private AlibabaJsonSerializer jsonSerializer = new AlibabaJsonSerializer();

    public void push(String module, String method, Object... params) throws Exception {
        pushWithServer(ServerConfigure.getServer(), module, method, params);
    }

    public void pushWithServer(String server, String module, String method, Object... params) throws Exception {
        if (async) {
            JSONObject json = new JSONObject();
            json.put("module", module);
            json.put("method", method);
            json.put("params", objArray2JSONArray(params));
            queue.offer(buildKey(server), json.toString());
        } else {
            if (ServerConfigure.isDevEnv()) {
                //TODO 临时修改--等贺群斐确认---同步消费应该也是读取module-asyn.xml文件而不是module.xml？
                INaviModuleContext moduleCtx = NaviAsyncModuleContextFactory.getInstance().getNaviAsyncModuleContext(module);
                INaviAsynchronousMethod asynMethod = (INaviAsynchronousMethod) moduleCtx.getBean(method);
                List<String[]> paramsList = new ArrayList<>();
                paramsList.add(objArray2StringArray(params));
                asynMethod.invoke(paramsList);
                return;
            }
            INaviModuleContext moduleCtx = NaviModuleContextFactory.getInstance().getNaviModuleContext(module);
            INaviAsynchronousMethod asynMethod = (INaviAsynchronousMethod) moduleCtx.getBean(method);
            List<String[]> paramsList = new ArrayList<>();
            paramsList.add(objArray2StringArray(params));
            asynMethod.invoke(paramsList);
        }

    }

    private String[] objArray2StringArray(Object[] objs) {
        String[] strs = new String[objs.length];
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] != null) {
                strs[i] = jsonSerializer.getJSONString(objs[i]);
            } else {
                strs[i] = null;
            }
        }
        return strs;
    }

    private JSONArray objArray2JSONArray(Object[] objs) {
        JSONArray array = new JSONArray();
        for (Object obj : objs) {
            if (obj != null) {
                if (obj instanceof String) {
                    array.add(obj);
                } else {
                    String str = jsonSerializer.getJSONString(obj);
                    try {
                        array.add(JSON.parseObject(str));
                    } catch (JSONException e) {
                        array.add(str);
                    }
                }
            } else {
                array.add("null");
            }
        }
        return array;
    }

    private String buildKey(String service) {
        return "JavaNavi:AsyncMQ:" + service;
    }

}
