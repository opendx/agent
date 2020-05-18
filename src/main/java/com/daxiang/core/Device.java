package com.daxiang.core;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.App;
import com.daxiang.model.FileType;
import com.daxiang.model.UploadFile;
import com.daxiang.model.page.Page;
import com.daxiang.server.ServerClient;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by jiangyitao.
 */
public abstract class Device {

    public static final int OFFLINE_STATUS = 0;
    public static final int USING_STATUS = 1;
    public static final int IDLE_STATUS = 2;

    protected String agentIp;
    protected int agentPort;

    protected DeviceTestTaskExecutor deviceTestTaskExecutor;
    protected DeviceServer deviceServer;
    protected RemoteWebDriver driver;
    protected ServerClient serverClient;

    public Device(DeviceServer deviceServer) {
        this.deviceServer = deviceServer;
        deviceTestTaskExecutor = new DeviceTestTaskExecutor(this);
        serverClient = ServerClient.getInstance();

        agentIp = App.getProperty("ip");
        agentPort = Integer.parseInt(App.getProperty("port"));
    }

    public RemoteWebDriver getDriver() {
        return driver;
    }

    public DeviceServer getDeviceServer() {
        return deviceServer;
    }

    public DeviceTestTaskExecutor getDeviceTestTaskExecutor() {
        return deviceTestTaskExecutor;
    }

    public abstract String getId();

    public abstract Integer getStatus();

    public abstract RemoteWebDriver newDriver(JSONObject caps);

    public abstract void onlineToServer();

    public abstract void idleToServer();

    public abstract void usingToServer(String username);

    public abstract void offlineToServer();

    public void startRecordingScreen() {
        throw new UnsupportedOperationException();
    }

    public File stopRecordingScreen() throws IOException {
        throw new UnsupportedOperationException();
    }

    public UploadFile stopRecordingScreenThenUploadToServer() throws IOException {
        File video = stopRecordingScreen();
        try {
            return serverClient.uploadFile(video, FileType.VIDEO);
        } finally {
            FileUtils.deleteQuietly(video);
        }
    }

    public boolean isConnected() {
        Integer status = getStatus();
        return status != null && status != OFFLINE_STATUS;
    }

    public boolean isIdle() {
        return getStatus() == IDLE_STATUS;
    }

    public RemoteWebDriver freshDriver(JSONObject caps) {
        quitDriver();
        driver = newDriver(caps);
        return driver;
    }

    public void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ign) {
            }
        }
    }

    public Map<String, Object> dump() {
        String pageSource = driver.getPageSource();
        return ImmutableMap.of("type", Page.TYPE_WEB, "pageSource", pageSource);
    }

    public File screenshot() {
        return driver.getScreenshotAs(OutputType.FILE);
    }

    public UploadFile screenshotThenUploadToServer() {
        File screenshotFile = screenshot();
        try {
            return ServerClient.getInstance().uploadFile(screenshotFile, FileType.IMG);
        } finally {
            FileUtils.deleteQuietly(screenshotFile);
        }
    }
}
