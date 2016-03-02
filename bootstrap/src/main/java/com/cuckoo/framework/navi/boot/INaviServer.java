package com.cuckoo.framework.navi.boot;

import java.util.Properties;

public interface INaviServer {

    int SUCCESS = 0;
    int FAIL = 1;

    int setupServer(Properties serverCfg);

    void stopServer();

}
