package com.youku.java.navi.server.module;

import com.youku.java.navi.boot.NaviDefine;
import com.youku.java.navi.common.RestApi;
import com.youku.java.navi.server.ServerConfigure;
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

    private NaviModuleContextFactory() {
    }

    /**
     * 静态初始化器，由JVM来保证线程安全
     */
    private static class SingletonHolder {
        public static final NaviModuleContextFactory instance = new NaviModuleContextFactory();
    }

    private final static int DELAY = 0; // 0s延迟

    private Map<String, INaviModuleContext> map = new HashMap<>();
    private Map<String, Map<String, RestApi>> restMap = new HashMap<>();  // <module_name, <uri, rest>>

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
        for (INaviModuleContext context : map.values()) {
            String moduleNm = context.getModuleName();
            if (context.getContextStatus().equals(INaviModuleContext.ContextStatus.REFRESHING) || context.getContextStatus().equals(INaviModuleContext.ContextStatus.PREPARING)) {
                if (!restMap.containsKey(moduleNm)) {
                    restMap.put(moduleNm, new HashMap<String, RestApi>());
                }

                restMap.get(moduleNm).put(uri.toLowerCase(), api);
                return;
            }
        }

        log.error("rest map module not found, " + uri);
    }

    public RestApi getRestApi(String moduleNm, String uri) {
        if (!restMap.containsKey(moduleNm)) {
            return null;
        }

        return restMap.get(moduleNm).get(uri.toLowerCase());
    }

    public void startCheckModuleProccess() {
        if (!ServerConfigure.isDevEnv()) {
            ScheduledExecutorService exe = Executors.newScheduledThreadPool(1);
            exe.scheduleAtFixedRate(new CheckModuleVersion(), DELAY, ServerConfigure.getModuleLoadInterval(), TimeUnit.SECONDS);
        }
    }

    public INaviModuleContext getNaviModuleContext(String module) throws Exception {
        if (!map.containsKey(module)) {
            INaviModuleContext context;
            if (ServerConfigure.isDevEnv()) {
                context = new NaviDevModuleContext(module);
            } else if (ServerConfigure.isDaemonEnv()) {
                context = new NaviDaemonModuleContext(module);
            } else {
                throw new FileNotFoundException("no module file:" + module);
            }

            map.put(module, context);

            try {
                context.initModule();
            } catch (Exception e) {
                log.error("{}", e.getMessage());
                map.remove(module);
            }
        }

        return map.get(module);
    }

    public static NaviModuleContextFactory getInstance() {
        return SingletonHolder.instance;
    }

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
                        NaviModuleContext ctx = new NaviModuleContext(module.getName());
                        try {
                            map.put(module.getName(), ctx);
                            ctx.initModule();
                            log.info("module " + module.getName() + " has been loaded.");
                        } catch (Exception e) {
                            map.remove(module.getName());
                            log.info("module " + module.getName() + " init failed, " + e.getMessage());
                        }
                    } else {
                        INaviModuleContext mdl = map.get(module.getName()).refresh();
                        if (mdl != null) {
                            // 未刷新则不用更新
                            map.put(module.getName(), mdl);
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
