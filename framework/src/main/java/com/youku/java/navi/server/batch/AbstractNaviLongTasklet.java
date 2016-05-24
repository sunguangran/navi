package com.youku.java.navi.server.batch;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractNaviLongTasklet implements Tasklet, InitializingBean, DisposableBean {

    private boolean init = true;

    @Setter
    @Getter
    private int sleepTime = 2000;
    private final Object lock = new Object();
    private boolean stop = false;
    private Logger log = Logger.getLogger(AbstractNaviLongTasklet.class);

    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        if (init) {
            init();
            log.info("the batch process inits completely.");
            init = false;
        }

        try {
            contribution.setExitStatus(mapResult(execute(chunkContext)));
            Thread.sleep(getSleepTime());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            synchronized (lock) {
                if (stop) {
                    lock.notifyAll();
                    log.info("business is executed compeletly!");

                    return RepeatStatus.FINISHED;
                }
            }
        }

        return RepeatStatus.CONTINUABLE;
    }

    /**
     * 初始化一次
     */
    public abstract void init();

    /**
     * 循环执行，间隔时间sleepTime,毫秒
     *
     * @param chunkContext
     * @return
     * @throws Exception
     */
    public abstract ExitStatus execute(ChunkContext chunkContext) throws Exception;

    private ExitStatus mapResult(Object result) {
        if (result instanceof ExitStatus) {
            return (ExitStatus) result;
        }
        return ExitStatus.COMPLETED;
    }

    public void destroy() throws Exception {
        synchronized (lock) {
            stop = true;
            lock.wait();
            log.info("start destorying!");
        }
        
        toDestory();
        log.info("the batch process will be stopped.");
    }

    /**
     * batch程序停止前回收资源
     *
     * @throws Exception
     */
    public abstract void toDestory() throws Exception;

}
