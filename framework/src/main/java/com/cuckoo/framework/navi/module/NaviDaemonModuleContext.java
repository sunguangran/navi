package com.cuckoo.framework.navi.module;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class NaviDaemonModuleContext implements INaviModuleContext {

    private String moduleNm;
    private String confPath;
    private ClassPathXmlApplicationContext cxt;

    public NaviDaemonModuleContext(String moduleNm) {
        this.moduleNm = moduleNm;
        confPath = NaviModulesUtil.getModuleConfPath(moduleNm);
    }

    public Object getBean(String apiNm) throws Exception {
        if (cxt == null) {
            throw new Exception("module not init!");
        }
        return cxt.getBean(apiNm);
    }

    public INaviModuleContext initModule() throws Exception {
        cxt = new ClassPathXmlApplicationContext(new String[]{"file:" + confPath}, false);
        // ClassLoader cl = this.getClass().getClassLoader();
        // Thread.currentThread().setContextClassLoader(cl);
        cxt.setClassLoader(new NaviModuleClassLoader(this.getClass().getClassLoader(), moduleNm));
        cxt.registerShutdownHook();// jvm退出时，回收资源
        cxt.refresh();
        // Thread.currentThread().setContextClassLoader(cl);
        return this;
    }

    public INaviModuleContext refresh() throws Exception {
        return null;
    }

    public void close() throws Exception {
        if (cxt == null) {
            throw new Exception("module not init!");
        }
        cxt.close();
    }

    @Override
    public String getBeanId(Class<?> clazz) throws Exception {
        return null;
    }
}
