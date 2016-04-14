package com.youku.java.navi.engine.async;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Setter
@Getter
public class NaviAsyncExecutorFactoryBean implements FactoryBean<ThreadPoolExecutor> {

    private int corePoolSize = 0;
    private long keepAliveTime = 60L;
    private int maximumPoolSize = Runtime.getRuntime().availableProcessors() * 2;

    public ThreadPoolExecutor getObject() throws Exception {

        RejectedExecutionHandler handler = new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r,
                                          ThreadPoolExecutor executor) {
                if (!executor.isShutdown()) {
                    try {
                        executor.getQueue().put(r);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        return new ThreadPoolExecutor(
            corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), handler
        );
    }

    public Class<?> getObjectType() {
        return ThreadPoolExecutor.class;
    }

    public boolean isSingleton() {
        return true;
    }

}
