package com.youku.java.navi.engine.async;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youku.java.navi.engine.core.INaviMQConsumeStrategy;
import com.youku.java.navi.server.module.INaviModuleContext;
import com.youku.java.navi.server.module.NaviAsyncModuleContextFactory;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class AsyncMethodConsumeStrategy implements INaviMQConsumeStrategy<String> {

    private ThreadPoolExecutor executor;

    private Logger log = Logger.getLogger(AsyncMethodConsumeStrategy.class);

    public Class<String> getClassNM() {
        return String.class;
    }

    public void afterPropertiesSet() throws Exception {

    }

    public void destroy() throws Exception {
        executor.shutdown();
    }

    public void consume(List<String> list) {
        Map<INaviAsynchronousMethod, List<String[]>> map = new HashMap<INaviAsynchronousMethod, List<String[]>>();
        for (String node : list) {
            try {
                JSONObject obj = JSON.parseObject(node);
                String module = obj.getString("module");
                String method = obj.getString("method");
                JSONArray jsonParams = obj.getJSONArray("params");
                INaviModuleContext moduleCtx = NaviAsyncModuleContextFactory.getInstance()
                    .getNaviAsyncModuleContext(module);
                INaviAsynchronousMethod asynMethod = (INaviAsynchronousMethod) moduleCtx.getBean(method);
                String[] params = parseParams(jsonParams);
                if (map.containsKey(asynMethod)) {
                    map.get(asynMethod).add(params);
                } else {
                    if (!asynMethod.getBatch()) {
                        executor.execute(new AsyncRunner(asynMethod, params));
                    } else {
                        map.put(asynMethod, new ArrayList<String[]>());
                        map.get(asynMethod).add(params);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        if (map.size() > 0) {
            for (INaviAsynchronousMethod method : map.keySet()) {
                executor.execute(new AsyncRunner(method, map.get(method)));
            }
        }
    }

    private String[] parseParams(JSONArray params) {
        String[] strs = new String[params.size()];
        for (int i = 0; i < params.size(); i++) {
            String str = params.get(i).toString();
            if ("null".equals(str)) {
                strs[i] = null;
            } else {
                strs[i] = str;
            }
        }
        return strs;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    class AsyncRunner implements Runnable {

        private List<String[]> params;
        private INaviAsynchronousMethod method;

        public AsyncRunner(INaviAsynchronousMethod method, String[] param) {
            this.method = method;
            this.params = new ArrayList<String[]>();
            this.params.add(param);
        }

        public AsyncRunner(INaviAsynchronousMethod method, List<String[]> params) {
            this.method = method;
            this.params = params;
        }

        public void run() {
            int tryTime = 0;
            int retry = 0;
            do {
                try {
                    method.invoke(params);
                } catch (NaviAsyncRetryException e) {
                    log.error(e.getCause().getMessage(), e);
                    retry = e.getTimes();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            } while (tryTime++ < retry);

        }

    }
}
