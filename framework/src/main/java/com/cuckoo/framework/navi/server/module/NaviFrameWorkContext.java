package com.cuckoo.framework.navi.server.module;

import com.cuckoo.framework.navi.boot.NaviDefine;
import com.cuckoo.framework.navi.boot.NaviServerClassloader;

public class NaviFrameWorkContext implements INaviModuleContext {

    private String confPath;
    private NaviClassPathXmlApplicationContext ctx;

    public NaviFrameWorkContext(String file) {
        confPath = NaviDefine.NAVI_HOME + "/conf/" + file + ".xml";
    }

    public Object getBean(String name) throws Exception {
        return ctx.getBean(name);
    }

    public INaviModuleContext initModule() throws Exception {
        ctx = new NaviClassPathXmlApplicationContext(new String[]{"file:" + confPath}, false);
        ctx.refresh();
        ctx.registerShutdownHook();
        return this;
    }

    public INaviModuleContext refresh() throws Exception {
        NaviClassPathXmlApplicationContext nctx = new NaviClassPathXmlApplicationContext(new String[]{"file:" + confPath}, false);
        nctx.setClassLoader(new NaviServerClassloader());
        nctx.refresh();
        nctx.registerShutdownHook();
        NaviClassPathXmlApplicationContext tcxt = ctx;
        ctx = nctx;
        tcxt.prepareClose();
        tcxt.setClassLoader(null);
        tcxt = null;
        return this;
    }

    public void close() throws Exception {
        ctx.close();
    }

    @Override
    public String getBeanId(Class<?> clazz) throws Exception {
        return null;
    }
}
