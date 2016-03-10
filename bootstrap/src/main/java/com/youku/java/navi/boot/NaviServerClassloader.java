package com.youku.java.navi.boot;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

/**
 * 服务类加载器
 *
 */
public class NaviServerClassloader extends NaviJarClassLoader {

    public NaviServerClassloader() throws FileNotFoundException,
        MalformedURLException {
        super(NaviDefine.NAVI_LIBS);
        loadJarFiles();
    }

    public NaviServerClassloader(ClassLoader parent) throws MalformedURLException, FileNotFoundException {
        super(parent, NaviDefine.NAVI_LIBS);
        loadJarFiles();
    }
}
