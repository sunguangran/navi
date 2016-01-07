package com.cuckoo.framework.navi.boot;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * navi default classloader
 */
public class NaviJarClassLoader extends URLClassLoader {

    private String libsPath = null;

    public NaviJarClassLoader(URL[] urls, String libsPath) {
        super(urls);
        this.libsPath = libsPath;
    }

    public NaviJarClassLoader(ClassLoader parent, String libsPath) {
        super(new URL[0], parent);
        this.libsPath = libsPath;
    }

    public NaviJarClassLoader(String libsPath) {
        this(new URL[0], libsPath);
    }

    /**
     * Adds the jar file with the following url into the class loader. This can
     * be a local or network resource.
     *
     * @param url
     *     The url of the jar file i.e. http://www.xxx.yyy/jarfile.jar or
     *     file:c:\foo\lib\testbeans.jar
     */
    public void addJarFile(URL url) {
        addURL(url);
    }

    /**
     * Adds a jar file from the filesystems into the jar loader list.
     *
     * @param localPath
     *     The full path to the jar file.
     * @throws MalformedURLException
     */
    public void addJarFile(String localPath) throws MalformedURLException {
        URL url = new URL("file:" + localPath);
        addURL(url);
    }

    protected void loadJarFiles() throws MalformedURLException, FileNotFoundException {
        File file = new File(libsPath);
        if (!file.exists() || !file.isDirectory()) {
            throw new FileNotFoundException("classloader path " + libsPath + " isn't exist!");
        }

        File[] jars = file.listFiles();

        if (jars == null || jars.length == 0) {
            throw new FileNotFoundException("classloader path " + libsPath + " has no jar file!");
        }

        for (File jar : jars) {
            if (!jar.getName().endsWith(".jar")) {
                continue;
            }

            addJarFile(jar.getAbsolutePath());
        }
    }
}
