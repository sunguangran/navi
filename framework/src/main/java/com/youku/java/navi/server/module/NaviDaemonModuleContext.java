package com.youku.java.navi.server.module;

public class NaviDaemonModuleContext implements INaviModuleContext {

    private String moduleNm;
    private String confPath;
    private NaviClassPathXmlApplicationContext cxt;

    protected ContextStatus status = ContextStatus.INITIAL;

    public NaviDaemonModuleContext(String moduleNm) {
        this.moduleNm = moduleNm;
        confPath = NaviModulesUtil.getModuleConfPath(moduleNm);
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
            throw new Exception("module not init!");
        }

        return cxt.getBean(apiNm);
    }

    public INaviModuleContext initModule() throws Exception {
        try {
            status = ContextStatus.PREPARING;
            cxt = new NaviClassPathXmlApplicationContext(new String[]{"file:" + confPath}, false);
            // ClassLoader cl = this.getClass().getClassLoader();
            // Thread.currentThread().setContextClassLoader(cl);
            cxt.setClassLoader(new NaviModuleClassLoader(this.getClass().getClassLoader(), moduleNm));
            cxt.registerShutdownHook();// jvm退出时，回收资源
            status = ContextStatus.PREPARING;
            cxt.refresh();
            status = ContextStatus.NORMAL;
            // Thread.currentThread().setContextClassLoader(cl);
        } finally {
            status = ContextStatus.NORMAL;
        }

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
