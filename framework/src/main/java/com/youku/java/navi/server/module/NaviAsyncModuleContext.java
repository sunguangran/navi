package com.youku.java.navi.server.module;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Navi部署环境module上下文
 */
@Slf4j
public class NaviAsyncModuleContext implements INaviModuleContext {

    private NaviClassPathXmlApplicationContext cxt;
    private String moduleNm;
    // private long cfgLastModified;
    private String confPath = null;
    private String confDir = null;
    private long libsLastModified;
    private String libsPath = null;
    private Map<String, Long> confTimeMap;

    protected ContextStatus status = ContextStatus.INITIAL;

    public NaviAsyncModuleContext(String moduleNm) {
        this.moduleNm = moduleNm;
        confPath = NaviModulesUtil.getModuleConfDir(moduleNm) + moduleNm + "-async.xml";
        confDir = NaviModulesUtil.getModuleConfDir(moduleNm);
        libsPath = NaviModulesUtil.getModuleLibsPath(moduleNm);
        confTimeMap = new HashMap<>();
    }

    @Override
    public String getModuleName() {
        return this.moduleNm;
    }

    @Override
    public ContextStatus getContextStatus() {
        return status;
    }

    public INaviModuleContext initModule() throws FileNotFoundException, MalformedURLException {
        try {
            status = ContextStatus.PREPARING;
            initConfFilesTime();
            // cfgLastModified = new File(confPath).lastModified();
            libsLastModified = new File(libsPath).lastModified();
            cxt = new NaviClassPathXmlApplicationContext(new String[]{"file:" + confPath}, false);
            cxt.setClassLoader(new NaviModuleClassLoader(getClass().getClassLoader(), moduleNm));
            status = ContextStatus.PREPARING;
            cxt.refresh();
            status = ContextStatus.NORMAL;
            cxt.registerShutdownHook();// jvm退出时，回收资源
        } finally {
            status = ContextStatus.NORMAL;
        }

        return this;
    }

    private void initConfFilesTime() {
        File dir = new File(confDir);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files == null || files.length == 0) {
                log.warn("the module config files aren't exist!");
            } else {
                for (File file : files) {
                    confTimeMap.put(file.getName(), file.lastModified());
                }
            }
        } else {
            log.warn("the module config dir isn't exist!");
        }
    }

    public INaviModuleContext refresh() throws FileNotFoundException, MalformedURLException {
        try {
            status = ContextStatus.REFRESHING;

            boolean confFileModified = isConfFilesModified();
            // long cfgCurrModified = new File(confPath).lastModified();
            long libsCurrModified = new File(libsPath).lastModified();
            if (confFileModified || libsLastModified < libsCurrModified) {
                NaviClassPathXmlApplicationContext newCxt = new NaviClassPathXmlApplicationContext(new String[]{"file:" + confPath}, false);
                newCxt.setClassLoader(new NaviModuleClassLoader(getClass().getClassLoader(), moduleNm));
                status = ContextStatus.PREPARING;
                newCxt.refresh();
                status = ContextStatus.NORMAL;
                newCxt.registerShutdownHook();// jvm退出时，回收资源
                // cfgLastModified = cfgCurrModified;
                libsLastModified = libsCurrModified;
                NaviClassPathXmlApplicationContext tmpCxt = cxt;
                cxt = newCxt;
                tmpCxt.prepareClose();// 干掉过期的context,同时回收资源
                tmpCxt.setClassLoader(null);

                log.info("old spring container is closed!");
                return this;
            }
        } finally {
            status = ContextStatus.NORMAL;
        }
        
        return null;
    }

    private boolean isConfFilesModified() {
        File dir = new File(confDir);
        boolean modified = false;
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (!confTimeMap.containsKey(file.getName())) {
                    confTimeMap.put(file.getName(), file.lastModified());
                    // 不存在也刷新
                    modified = true;

                } else if (file.lastModified() > confTimeMap
                    .get(file.getName())) {
                    confTimeMap.put(file.getName(), file.lastModified());
                    modified = true;
                }
            }
        }
        return modified;
    }

    public Object getBean(String apiNm) throws Exception {
        if (cxt == null) {
            throw new Exception("module not init!");
        }
        return cxt.getBean(apiNm);
    }

    @Override
    public String getBeanId(Class<?> clazz) throws Exception {
        return null;
    }

    public void close() throws Exception {
        if (cxt == null) {
            throw new Exception("module not init!");
        }
        cxt.close();
    }
}
