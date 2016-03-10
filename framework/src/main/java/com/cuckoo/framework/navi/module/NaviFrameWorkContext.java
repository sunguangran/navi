package com.cuckoo.framework.navi.module;

import com.cuckoo.framework.navi.boot.NaviProps;
import com.cuckoo.framework.navi.boot.NaviServerClassloader;

public class NaviFrameWorkContext implements INaviModuleContext {

    private String confPath;
    private NaviClassPathXmlApplicationContext cxt;

    public NaviFrameWorkContext(String file) {
        confPath = NaviProps.NAVI_HOME + "/conf/" + file + ".xml";
    }

    public Object getBean(String name) throws Exception {
        return cxt.getBean(name);
    }

    public INaviModuleContext initModule() throws Exception {
        cxt = new NaviClassPathXmlApplicationContext(new String[]{"file:" + confPath}, false);
        cxt.refresh();
        cxt.registerShutdownHook();
        return this;
    }

    public INaviModuleContext refresh() throws Exception {
        NaviClassPathXmlApplicationContext ncxt = new NaviClassPathXmlApplicationContext(new String[]{"file:" + confPath}, false);
        ncxt.setClassLoader(new NaviServerClassloader());
        ncxt.refresh();
        ncxt.registerShutdownHook();
        NaviClassPathXmlApplicationContext tcxt = cxt;
        cxt = ncxt;
        tcxt.prepareClose();
        tcxt.setClassLoader(null);
        tcxt = null;
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
