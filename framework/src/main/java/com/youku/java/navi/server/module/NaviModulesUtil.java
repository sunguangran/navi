package com.youku.java.navi.server.module;

import com.youku.java.navi.server.ServerConfigure;

public class NaviModulesUtil {

    public static String getModuleConfPath(String moduleNm) {
        return getModuleConfDir(moduleNm) + moduleNm + ".xml";
    }

    public static String getModuleConfDir(String moduleNm) {
        return ServerConfigure.NAVI_MODULES + moduleNm + "/conf/";
    }

    public static String getModuleLibsPath(String moduleNm) {
        return ServerConfigure.NAVI_MODULES + moduleNm + "/libs/";
    }

}
