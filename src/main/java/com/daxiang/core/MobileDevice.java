package com.daxiang.core;

import com.daxiang.server.ServerApi;
import com.daxiang.core.appium.AppiumDriverFactory;
import com.daxiang.core.appium.AppiumServer;
import com.daxiang.model.Device;
import com.daxiang.model.FileType;
import com.daxiang.model.UploadFile;
import io.appium.java_client.AppiumDriver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.openqa.selenium.OutputType;

import java.io.File;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Data
public abstract class MobileDevice {

    public static final String NATIVE_CONTEXT = "NATIVE_APP";

    public static final int ANDROID = 1;
    public static final int IOS = 2;

    private Device device;
    private DeviceTestTaskExecutor deviceTestTaskExecutor;

    private AppiumServer appiumServer;
    private AppiumDriver appiumDriver;

    public MobileDevice(Device device, AppiumServer appiumServer) {
        this.device = device;
        this.appiumServer = appiumServer;
        deviceTestTaskExecutor = new DeviceTestTaskExecutor(this);
    }

    public AppiumDriver initAppiumDriver() {
        appiumDriver = AppiumDriverFactory.initAppiumDriver(this);
        return appiumDriver;
    }

    public AppiumDriver freshAppiumDriver(Integer platform) {
        quitAppiumDriver();
        appiumDriver = AppiumDriverFactory.create(this, platform);
        return appiumDriver;
    }

    public void quitAppiumDriver() {
        if (appiumDriver != null) {
            try {
                appiumDriver.quit();
            } catch (Exception ign) {
            }
        }
    }

    public String getId() {
        return device.getId();
    }

    public boolean isConnected() {
        return device.getStatus() != Device.OFFLINE_STATUS;
    }

    public boolean isIdle() {
        return device.getStatus() == Device.IDLE_STATUS;
    }

    public boolean isNativeContext() {
        return NATIVE_CONTEXT.equals(appiumDriver.getContext());
    }

    public File screenshot() {
        return appiumDriver.getScreenshotAs(OutputType.FILE);
    }

    public UploadFile screenshotAndUploadToServer() {
        File screenshotFile = screenshot();
        try {
            return ServerApi.getInstance().uploadFile(screenshotFile, FileType.IMG);
        } finally {
            FileUtils.deleteQuietly(screenshotFile);
        }
    }

    public abstract void installApp(File appFile) throws Exception;

    public abstract void uninstallApp(String app) throws Exception;

    public abstract String dump() throws IOException, DocumentException;

    public abstract boolean acceptAlert();

    public abstract boolean dismissAlert();

    public abstract void startRecordingScreen();

    public abstract File stopRecordingScreen() throws IOException;

    public UploadFile stopRecordingScreenAndUploadToServer() throws IOException {
        File video = stopRecordingScreen();
        try {
            return ServerApi.getInstance().uploadFile(video, FileType.VIDEO);
        } finally {
            FileUtils.deleteQuietly(video);
        }
    }

    public boolean isAndroid() {
        return device.getPlatform() == ANDROID;
    }
}