package com.youku.java.navi.server.module;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.FileNotFoundException;

public class NaviDevFrameWorkContext implements INaviModuleContext {

    private String fileNm;
    private ClassPathXmlApplicationContext cxt;
    private Logger logger = Logger.getLogger(NaviDevFrameWorkContext.class);

    public NaviDevFrameWorkContext(String moduleNm) {
        this.fileNm = moduleNm;
    }

    @Override
    public String getModuleName() {
        return null;
    }

    public Object getBean(String apiNm) throws Exception {
        if (cxt == null) {
            throw new Exception("async.xml not exists!");
        }
        return cxt.getBean(apiNm);
    }

    @Override
    public String getBeanId(Class<?> clazz) throws Exception {
        return null;
    }

    public INaviModuleContext initModule() {
        try {
            cxt = new NaviClassPathXmlApplicationContext(new String[]{fileNm + ".xml"}, true);
        } catch (BeanDefinitionStoreException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                logger.warn(fileNm + ".xml not exists!");
            }
        }
        return this;
    }

    public INaviModuleContext refresh() throws Exception {
        // TODO Auto-generated method stub
        return this;
    }

    public void close() throws Exception {
        if (cxt == null) {
            throw new Exception("async.xml not exists!");
        }
        cxt.close();
    }

    @Override
    public ContextStatus getContextStatus() {
        return null;
    }
}
