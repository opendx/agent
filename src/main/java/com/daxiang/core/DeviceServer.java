package com.daxiang.core;

import java.net.URL;

/**
 * Created by jiangyitao.
 */
public abstract class DeviceServer {

    protected boolean isRunning = false;
    protected URL url;

    public abstract void start();

    public abstract void stop();

    public URL getUrl() {
        if (!isRunning) {
            throw new IllegalStateException("device server未运行");
        }
        if (url == null) {
            throw new IllegalStateException("url is null");
        }

        return url;
    }
}
