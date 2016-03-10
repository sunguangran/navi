package com.cuckoo.framework.navi.boot;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NaviShutdownHook extends Thread {

    private INaviServer server;

    public NaviShutdownHook(INaviServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        log.info("navi server detected jvm shutdown, server will exit.");
        server.stopServer();
    }
}
