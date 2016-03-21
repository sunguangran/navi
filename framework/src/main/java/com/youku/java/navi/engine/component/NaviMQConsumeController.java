package com.youku.java.navi.engine.component;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NaviMQConsumeController implements DisposableBean, InitializingBean {

    @Setter @Getter
    private List<NaviMQConsumeTask> tasks;
    private ExecutorService executor;

    public NaviMQConsumeController() {
        executor = Executors.newCachedThreadPool();
    }

    public void start() {
        for (NaviMQConsumeTask task : tasks) {
            for (int i = 0; i < task.getContext().getThreadRate(); i++) {
                executor.execute(task);
            }
        }
    }

    public void afterPropertiesSet() throws Exception {
        if (tasks == null || tasks.size() == 0) {
            throw new NoSuchElementException("Tasks can't be null!");
        }
    }

    public void destroy() throws Exception {
        tasks.clear();
        if (executor != null) {
            executor.shutdown();
        }
    }

}
