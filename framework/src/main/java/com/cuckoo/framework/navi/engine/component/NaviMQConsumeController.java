package com.cuckoo.framework.navi.engine.component;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("rawtypes")
public class NaviMQConsumeController implements DisposableBean, InitializingBean {


    private List<NaviMQConsumeTask> tasks;
    private ExecutorService executor;

    public List<NaviMQConsumeTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<NaviMQConsumeTask> tasks) {
        this.tasks = tasks;
    }

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
