package com.cuckoo.framework.navi.boot;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@Slf4j
public abstract class ANaviMain {

    static {
        try {
            if (StringUtils.isEmpty(System.getProperty("NAVI_HOME")) && StringUtils.isNotEmpty(System.getenv("NAVI_HOME"))) {
                System.setProperty("NAVI_HOME", System.getenv("NAVI_HOME"));
            }

            // 初始化logback日志相关配置
            LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(logCtx);
            logCtx.reset();

            if (NaviDefine.NAVI_HOME != null) {
                configurator.doConfigure(NaviDefine.NAVI_LOGBACK_PATH);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析框架服务配置文件
     */
    protected Properties parseConfig(String cfgPath) {
        log.info("start parsing config file.");

        Properties props = new Properties();
        try {
            File cfgFile = new File(cfgPath);
            BufferedReader reader = new BufferedReader(new FileReader(cfgFile));
            String tmp;
            while ((tmp = reader.readLine()) != null) {
                if (tmp.startsWith("#") || tmp.length() == 0) {
                    continue;
                }

                String[] pairs = tmp.split("=");
                if (pairs.length > 1) {
                    props.put(pairs[0].trim(), pairs[1].trim());
                }
            }

            reader.close();
        } catch (IOException e) {
            log.error("{}", e.getMessage());
        }

        return props;
    }


    /**
     * 获得启动类
     */
    public abstract String getStartClass(Properties serverConfig);

    /**
     * 获得服务配置文件地址
     */
    public abstract String getConfPath();

    /**
     * 构建启动配置对象
     */
    public Properties parseServerConfig(String[] args) {
        return parseConfig(getConfPath());
    }

    protected void doMain(Properties serverConfig) throws Exception {
        NaviServerClassloader loader = new NaviServerClassloader(Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(loader);

        INaviServer server = (INaviServer) Class.forName(getStartClass(serverConfig), true, loader).newInstance();
        int statCode = server.setupServer(serverConfig);
        if (statCode == INaviServer.SUCCESS) {
            Runtime.getRuntime().addShutdownHook(new NaviShutdownHook(server));
        } else {
            System.exit(statCode);
        }
    }
}
