package com.daxiang.core.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by jiangyitao.
 */
public class AgentClassLoader extends URLClassLoader {

    public AgentClassLoader() {
        super(new URL[0], Thread.currentThread().getContextClassLoader());
    }

    public void addJar(File jar) {
        try {
            super.addURL(jar.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
