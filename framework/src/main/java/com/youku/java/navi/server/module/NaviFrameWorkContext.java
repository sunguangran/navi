package com.youku.java.navi.server.module;

import com.youku.java.navi.boot.NaviDefine;
import com.youku.java.navi.boot.NaviServerClassloader;

public class NaviFrameWorkContext implements INaviModuleContext {

    private String confPath;
    private NaviClassPathXmlApplicationContext cxt;
    protected ContextStatus status = ContextStatus.INITIAL;

    public NaviFrameWorkContext(String file) {
        confPath = NaviDefine.NAVI_HOME + "/conf/" + file + ".xml";
    }

    @Override
    public String getModuleName() {
        return "framework";
    }

    @Override
    public ContextStatus getContextStatus() {
        return status;
    }

    public Object getBean(String name) throws Exception {
        return cxt.getBean(name);
    }

    public INaviModuleContext initModule() throws Exception {
        try {
            status = ContextStatus.PREPARING;
            cxt = new NaviClassPathXmlApplicationContext(new String[]{"file:" + confPath}, false);
            cxt.refresh();
            cxt.registerShutdownHook();
        } finally {
            status = ContextStatus.NORMAL;
        }

        return this;
    }

    public INaviModuleContext refresh() throws Exception {
        try {
            status = ContextStatus.REFRESHING;
            NaviClassPathXmlApplicationContext ncxt = new NaviClassPathXmlApplicationContext(new String[]{"file:" + confPath}, false);
            ncxt.setClassLoader(new NaviServerClassloader());
            ncxt.refresh();
            ncxt.registerShutdownHook();
            NaviClassPathXmlApplicationContext tcxt = cxt;
            cxt = ncxt;
            tcxt.prepareClose();
            tcxt.setClassLoader(null);
            tcxt = null;
        } finally {
            status = ContextStatus.NORMAL;
        }

        return this;
    }

    public void close() throws Exception {
        cxt.close();
    }

    @Override
    public String getBeanId(Class<?> clazz) throws Exception {
        return null;
    }
}
