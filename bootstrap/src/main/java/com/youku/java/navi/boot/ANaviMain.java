package com.youku.java.navi.boot;

import ch.qos.logback.classic.BasicConfigurator;
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

            // 初始化Logback
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            lc.reset();

            if (NaviDefine.NAVI_HOME != null) {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(lc);
                configurator.doConfigure(NaviDefine.NAVI_LOGBACK_PATH);
            } else {
                BasicConfigurator.configure(lc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析服务配置文件
     */
    protected Properties parseConfig(String confFile) {
        log.info("start parsing config file.");

        Properties props = new Properties();
        try {
            File file = new File(confFile);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String str;
            while ((str = br.readLine()) != null) {
                if (str.startsWith("#") || str.length() == 0) {
                    continue;
                }
                String[] pairs = str.split("=");
                if (pairs.length > 1) {
                    props.put(pairs[0].trim(), pairs[1].trim());
                }
            }
            br.close();
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
     * 获得配置地址
     */
    public abstract String getConfPath();

    /**
     * 构建启动配置对象
     */
    public Properties parseServerConfig(String[] args) {
        // 开发模式使用默认配置
        if (NaviDefine.NAVI_HOME == null) {
            log.warn("NAVI_HOME not defined, will use default config");

            Properties serverCfg = new Properties();
            serverCfg.setProperty(NaviDefine.PORT, NaviDefine.DEFAULT_PORT);
            serverCfg.setProperty(NaviDefine.SERVER, NaviDefine.DEFAULT_SERVER);
            serverCfg.setProperty(NaviDefine.CHUNK_AGGR_SIZE, NaviDefine.DEFAULT_CHUNK_SIZE);

            return serverCfg;
        } else {
            return parseConfig(getConfPath());
        }
    }

    protected void doMain(Properties serverConfig) throws Exception {
        String mode = serverConfig.getProperty(NaviDefine.MODE);
        int statCode;
        final INaviServer server;
        if (NaviDefine.WORK_MODE.DEV == NaviDefine.WORK_MODE.toEnum(mode)) {
            server = (INaviServer) Class.forName(getStartClass(serverConfig), true, Thread.currentThread().getContextClassLoader()).newInstance();
        } else {
            NaviServerClassloader loader = new NaviServerClassloader(Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(loader);
            server = (INaviServer) Class.forName(getStartClass(serverConfig), true, loader).newInstance();
        }

        statCode = server.setupServer(serverConfig);
        if (statCode == INaviServer.SUCCESS) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    log.info("navi server detected jvm shutdown, server will exit.");
                    server.stopServer();
                }
            });
        } else {
            System.exit(statCode);
        }
    }
}
