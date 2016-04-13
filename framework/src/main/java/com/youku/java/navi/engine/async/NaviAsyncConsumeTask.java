package com.youku.java.navi.engine.async;

import com.youku.java.navi.engine.component.NaviMQConsumeTask;
import com.youku.java.navi.server.ServerConfigure;

public class NaviAsyncConsumeTask<T> extends NaviMQConsumeTask<T> {

    @Override
    public void afterPropertiesSet() throws Exception {
        if (getQueueKey() == null) {
            setQueueKey(buildQueueKey());
        }
        super.afterPropertiesSet();
    }

    private String buildQueueKey() {
        return "JavaNavi:AsyncMQ:" + ServerConfigure.getServer();
    }


}
