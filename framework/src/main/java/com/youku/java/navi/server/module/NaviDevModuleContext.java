package com.youku.java.navi.server.module;

import org.springframework.beans.factory.BeanDefinitionStoreException;

import java.io.FileNotFoundException;

/**
 * Navi开发环境模块上下文
 *
 * @author yaonb
 */
public class NaviDevModuleContext implements INaviModuleContext {

    private String moduleNm;
    private NaviClassPathXmlApplicationContext cxt;
    protected ContextStatus status = ContextStatus.INITIAL;

    public NaviDevModuleContext(String moduleNm) {
        this.moduleNm = moduleNm;
    }

    @Override
    public String getModuleName() {
        return this.moduleNm;
    }

    @Override
    public ContextStatus getContextStatus() {
        return status;
    }

    public Object getBean(String apiNm) throws Exception {
        if (cxt == null) {
            throw new Exception("not init module!");
        }
        return cxt.getBean(apiNm);
    }

    @Override
    public String getBeanId(Class<?> clazz) throws Exception {
        String[] beanNamesForType = cxt.getBeanNamesForType(clazz);
        if (beanNamesForType == null || beanNamesForType.length == 0) {
            return null;
        }

        return beanNamesForType[0];
    }

    public INaviModuleContext initModule() {
        try {
            status = ContextStatus.PREPARING;
            cxt = new NaviClassPathXmlApplicationContext(new String[]{moduleNm + ".xml"}, true);
        } catch (BeanDefinitionStoreException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new BeanDefinitionStoreException("no module " + moduleNm
                    + " is loaded!");
            }
            throw e;
        } finally {
            status = ContextStatus.NORMAL;
        }

        return this;
    }

    public INaviModuleContext refresh() throws Exception {
        // TODO Auto-generated method stub
        return this;
    }

    public void close() throws Exception {
        if (cxt == null) {
            throw new Exception("module not init!");
        }
        cxt.close();
    }
}
