package com.daxiang.core;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.net.UrlChecker;

import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
@Slf4j
public abstract class DeviceServer {

    protected boolean isRunning = false;
    protected URL url;
    protected Integer port;

    public abstract void start();

    public abstract void stop();

    public boolean isAvailable(int checkTimeoutInSeconds) {
        try {
            new UrlChecker()
                    .waitUntilAvailable(checkTimeoutInSeconds, TimeUnit.SECONDS, new URL(url.toString() + "/status"));
            return true;
        } catch (Exception e) {
            log.warn("{} is not availabe, check timeout: {} s", url, checkTimeoutInSeconds);
            return false;
        }
    }

    public void restart() {
        stop();
        start();
    }

    public URL getUrl() {
        if (!isRunning) {
            throw new IllegalStateException("device server未运行");
        }
        if (url == null) {
            throw new IllegalStateException("url is null");
        }

        return url;
    }

    public int getPort() {
        if (!isRunning) {
            throw new IllegalStateException("device server未运行");
        }
        if (port == null) {
            throw new IllegalStateException("port is null");
        }

        return port;
    }
}
