package com.cuckoo.framework.navi.server.module;

import com.cuckoo.framework.navi.boot.NaviDefine;
import com.cuckoo.framework.navi.common.RestApi;
import com.cuckoo.framework.navi.server.ServerConfigure;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class NaviModuleContextFactory {

    private final static NaviModuleContextFactory instance = new NaviModuleContextFactory();

    private final static int DELAY = 0; // 0s延迟

    private Map<String, INaviModuleContext> map = new HashMap<>();
    private Map<String, RestApi> restMap = new HashMap<>();

    public String getBeanId(String moduleNm, Class clazz) {
        INaviModuleContext context = map.get(moduleNm);
        if (context == null) {
            return null;
        }

        try {
            return context.getBeanId(clazz);
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            return null;
        }
    }

    public void addRestApi(String uri, RestApi api) {
        restMap.put(uri, api);
    }

    public RestApi getRestApi(String uri) {
        return restMap.get(uri);
    }

    public void startCheckModuleProccess() {
        ScheduledExecutorService exe = Executors.newScheduledThreadPool(1);
        exe.scheduleAtFixedRate(new CheckModuleVersion(), DELAY, ServerConfigure.getModuleLoadInterval(), TimeUnit.SECONDS);
    }

    public INaviModuleContext getNaviModuleContext(String module) throws Exception {
        if (!map.containsKey(module)) {
            throw new FileNotFoundException("module not found: " + module);
        }

        return map.get(module);
    }

    public static NaviModuleContextFactory getInstance() {
        return instance;
    }

    /**
     * 定期检查模块配置，热部署
     */
    private class CheckModuleVersion implements Runnable {

        private ReentrantLock lock = new ReentrantLock();

        public void run() {
            lock.lock();
            try {
                log.debug("checking modules version......");
                File folder = new File(ServerConfigure.NAVI_MODULES);
                if (!folder.exists()) {
                    if (!folder.mkdir()) {
                        throw new FileNotFoundException("module path not exists, '" + ServerConfigure.NAVI_MODULES + ".");
                    }
                    return;
                }

                File[] modules = folder.listFiles();
                if (modules == null || modules.length == 0) {
                    return;
                }

                for (File module : modules) {
                    if (module.isFile() || module.getName().startsWith(NaviDefine.PREFIX_NAVI_BATCH)) {
                        continue;
                    }

                    if (!map.containsKey(module.getName())) {
                        map.put(module.getName(), new NaviModuleContext(module.getName()).initModule());
                        log.info("module " + module.getName() + " has been loaded.");
                    } else {
                        INaviModuleContext context = map.get(module.getName()).refresh();
                        if (context != null) {
                            // 未刷新则不用更新
                            map.put(module.getName(), context);
                            log.info("module version of " + module.getName() + " has been updated successfully.");
                        }
                    }
                }

                log.debug("check modules version successfully.");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                lock.unlock();
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
