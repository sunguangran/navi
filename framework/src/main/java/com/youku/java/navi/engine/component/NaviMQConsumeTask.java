package com.youku.java.navi.engine.component;

import com.youku.java.navi.engine.core.IBaseDataService;
import com.youku.java.navi.engine.core.INaviMQConsumeStrategy;
import com.youku.java.navi.engine.core.INaviMessageQueue;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Setter
@Getter
public class NaviMQConsumeTask<T> implements Runnable, DisposableBean, InitializingBean {

    private IBaseDataService service;
    private INaviMessageQueue queue;
    private INaviMQConsumeStrategy<T> strategy;
    private NaviMQContext context;
    private String queueKey;
    private AtomicBoolean open = new AtomicBoolean(true);

    public void run() {
        while (open.get()) {
            try {
                List<T> list = new LinkedList<>();
                if (context.getConsumeRate() > 1) {
                    int size = queue.drainTo(queueKey, list, context.getConsumeRate(), strategy.getClassNM());
                    if (size <= 0) {
                        T obj = queue.poll(queueKey, context.getBlockTime(), strategy.getClassNM());
                        if (obj != null) {
                            list.add(obj);
                        }
                    }
                } else {
                    T obj = queue.poll(queueKey, context.getBlockTime(), strategy.getClassNM());
                    if (obj != null) {
                        list.add(obj);
                    }
                }
                strategy.consume(list);
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
                try {
                    Thread.sleep(context.getExceptionSleepTime());
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    public void stop() {
        open.set(false);
    }

    public void afterPropertiesSet() throws Exception {
        if (context == null) {
            context = new NaviMQContext();
        }

        queue = new NaviMessageQueueFactory().createMQ(service, queueKey, context.getMqEnumType());
    }

    public void destroy() throws Exception {
        stop();
    }

    public void setQueue(INaviMessageQueue queue) {
        this.queue = queue;
    }
}
