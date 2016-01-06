package com.cuckoo.framework.navi.module;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * 支持热部署下延时资源回收策略
 *
 */
@Slf4j
public class NaviClassPathXmlApplicationContext extends ClassPathXmlApplicationContext {

    protected final static String NAVI_URL_PREFIX = "navi:";

    public NaviClassPathXmlApplicationContext(String[] configs, boolean refresh) {
        super(configs, refresh);
    }

    public void prepareClose() {
        Thread thread = new Thread(new ClassPathXmlApplicationContextCloser(this), "hot-deploy-" + hashCode());
        thread.start();
    }

    @Override
    public void refresh() throws BeansException, IllegalStateException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());
        super.refresh();
        Thread.currentThread().setContextClassLoader(loader);
    }

    //支持"navi:"类型的resource
    @Override
    public Resource getResource(String location) {
        Assert.notNull(location, "Location must not be null");
        if (location.startsWith(NAVI_URL_PREFIX)) {
            return new FileSystemResource(getConfigPath() + location.substring(NAVI_URL_PREFIX.length()));
        } else {
            return super.getResource(location);
        }
    }

    private String getConfigPath() {
        String config = this.getConfigLocations()[0];
        return config.substring(5, config.lastIndexOf('/') + 1);
    }

    class ClassPathXmlApplicationContextCloser implements Runnable {

        private NaviClassPathXmlApplicationContext context;

        public ClassPathXmlApplicationContextCloser(
            NaviClassPathXmlApplicationContext context) {
            this.context = context;
        }

        public void run() {
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            }
            context.close();
        }
    }
}
