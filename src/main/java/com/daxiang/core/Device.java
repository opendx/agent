package com.daxiang.core;

import com.daxiang.App;
import com.daxiang.model.FileType;
import com.daxiang.model.UploadFile;
import com.daxiang.model.page.Page;
import com.daxiang.server.ServerClient;
import com.daxiang.utils.UUIDUtil;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by jiangyitao.
 */
@Slf4j
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

    protected Capabilities caps;

    public Device(DeviceServer deviceServer) {
        this.deviceServer = deviceServer;
        deviceTestTaskExecutor = new DeviceTestTaskExecutor(this);
        serverClient = ServerClient.getInstance();

        agentIp = App.getProperty("ip");
        agentPort = Integer.parseInt(App.getProperty("port"));
    }

    public DeviceServer getDeviceServer() {
        return deviceServer;
    }

    public RemoteWebDriver getDriver() {
        return driver;
    }

    public DeviceTestTaskExecutor getDeviceTestTaskExecutor() {
        return deviceTestTaskExecutor;
    }

    public abstract String getId();

    public abstract Integer getStatus();

    public void setCaps(Capabilities caps, boolean merge) {
        if (caps != null && !merge) {
            this.caps = caps;
        } else {
            this.caps = newCaps(caps);
        }
    }

    public RemoteWebDriver newDriver() {
        return new RemoteWebDriver(deviceServer.getUrl(), this.caps);
    }

    protected abstract Capabilities newCaps(Capabilities capsToMerge);

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

    public UploadFile stopRecordingScreenAndUploadToServer() throws IOException {
        File video = stopRecordingScreen();
        try {
            return serverClient.uploadFile(video, FileType.VIDEO);
        } finally {
            FileUtils.deleteQuietly(video);
        }
    }

    public File getLogFile(long startTime) throws IOException {
        String logType = getLogType();
        if (logType == null) {
            return null;
        }

        List<String> logStrings = driver.manage().logs().get(logType).getAll().stream()
                .filter(logEntry -> logEntry.getTimestamp() >= startTime)
                .map(LogEntry::getMessage).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(logStrings)) {
            return null;
        }

        File logFile = new File(UUIDUtil.getUUID() + ".txt");
        boolean isCreateLogFileSuccess = logFile.createNewFile();
        if (!isCreateLogFileSuccess) {
            log.warn("create logfile={} fail", logFile.getAbsolutePath());
            return null;
        }

        FileUtils.writeLines(logFile, "UTF-8", logStrings, true);
        return logFile;
    }

    public UploadFile getLogAndUploadToServer(long startTime) throws IOException {
        File logFile = getLogFile(startTime);
        if (logFile == null) {
            return null;
        }

        try {
            return serverClient.uploadFile(logFile, FileType.LOG);
        } finally {
            FileUtils.deleteQuietly(logFile);
        }
    }

    public boolean isConnected() {
        Integer status = getStatus();
        return status != null && status != OFFLINE_STATUS;
    }

    public boolean isIdle() {
        return getStatus() == IDLE_STATUS;
    }

    // codetemplate/index.ftl
    // 使用当前caps刷新driver
    public RemoteWebDriver freshDriver() {
        return freshDriver(caps, false);
    }

    public RemoteWebDriver freshDriver(Capabilities caps, boolean merge) {
        if (!deviceServer.isAvailable(30)) {
            deviceServer.restart();
        }

        quitDriver();
        setCaps(caps, merge);
        driver = newDriver();
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

    public UploadFile screenshotAndUploadToServer() {
        return screenshotAndUploadToServer(FileType.IMG);
    }

    public UploadFile screenshotAndUploadToServer(Integer fileType) {
        File screenshotFile = screenshot();
        try {
            return ServerClient.getInstance().uploadFile(screenshotFile, fileType);
        } finally {
            FileUtils.deleteQuietly(screenshotFile);
        }
    }

    public boolean acceptAlert() {
        try {
            driver.switchTo().alert().accept();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean dismissAlert() {
        try {
            driver.switchTo().alert().dismiss();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getLogType() {
        return null;
    }
}
