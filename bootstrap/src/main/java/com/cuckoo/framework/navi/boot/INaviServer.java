package com.cuckoo.framework.navi.boot;

import java.util.Properties;

public interface INaviServer {

    int FAIL = 1;
    int SUCCESS = 0;

    int setupServer(Properties serverCfg);

    void stopServer();

}
