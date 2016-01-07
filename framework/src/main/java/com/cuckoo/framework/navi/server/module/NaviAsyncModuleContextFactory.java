package com.cuckoo.framework.navi.server.module;

import com.cuckoo.framework.navi.boot.NaviDefine;
import com.cuckoo.framework.navi.server.ServerConfigure;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NaviAsyncModuleContextFactory {

    private final static NaviAsyncModuleContextFactory instance = new NaviAsyncModuleContextFactory();
    private final static int DELAY = 0;// 0s延迟

    private Map<String, INaviModuleContext> map = new HashMap<>();

    public void startCheckModuleProccess() {
        ScheduledExecutorService exe = Executors.newScheduledThreadPool(1);
        exe.scheduleAtFixedRate(new CheckModuleVersion(), DELAY, ServerConfigure.getModuleLoadInterval(), TimeUnit.SECONDS);
    }

    public INaviModuleContext getNaviAsyncModuleContext(String module) throws Exception {
        if (!map.containsKey(module)) {
            throw new FileNotFoundException("no async module file:" + module + "-async");
        }

        return map.get(module);
    }

    public static NaviAsyncModuleContextFactory getInstance() {
        return instance;
    }

    private class CheckModuleVersion implements Runnable {
        public void run() {
            try {
                log.debug("checking modules version......");
                loadNaviAsyncModuleContext();
                log.debug("checked modules version successfully!");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public synchronized void loadNaviAsyncModuleContext() throws Exception {
        File folder = new File(ServerConfigure.NAVI_MODULES);
        if (!folder.exists()) {
            return;
        }

        if (!folder.exists()) {
            throw new FileNotFoundException(ServerConfigure.NAVI_MODULES + " isn't exist!");
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            if (file.isFile() || file.getName().startsWith(NaviDefine.PREFIX_NAVI_BATCH)) {
                continue;
            }

            if (!map.containsKey(file.getName())) {
                String asyncPath = NaviModulesUtil.getModuleConfDir(file.getName()) + file.getName() + "-async.xml";
                if (new File(asyncPath).exists()) {
                    map.put(file.getName(), new NaviAsyncModuleContext(file.getName()).initModule());
                    log.info("async module " + file.getName() + " is loaded!");
                }
            } else {
                INaviModuleContext mdl = map.get(file.getName()).refresh();
                if (mdl != null) {
                    // 未刷新则不用更新
                    map.put(file.getName(), mdl);
                    log.info("async module " + file.getName() + " version is updated successfully!");
                }
            }
        }
    }

    public List<String> getNaviModuleNms() {
        Iterator<String> mSet = map.keySet().iterator();
        List<String> list = new ArrayList<>();
        while (mSet.hasNext()) {
            list.add(mSet.next());
        }
        return list;
    }
}
