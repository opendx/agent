package com.daxiang.core;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.model.Mobile;
import com.daxiang.model.page.Page;
import com.daxiang.server.ServerClient;
import com.daxiang.core.mobile.appium.AppiumDriverFactory;
import com.daxiang.core.mobile.appium.AppiumServer;
import com.daxiang.model.FileType;
import com.daxiang.model.UploadFile;
import com.google.common.collect.ImmutableMap;
import io.appium.java_client.AppiumDriver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.json.XML;
import org.openqa.selenium.OutputType;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Data
public abstract class MobileDevice {

    public static final String NATIVE_CONTEXT = "NATIVE_APP";

    public static final int ANDROID = 1;
    public static final int IOS = 2;

    protected Mobile mobile;
    protected DeviceTestTaskExecutor deviceTestTaskExecutor;

    protected AppiumServer appiumServer;
    protected AppiumDriver appiumDriver;

    public MobileDevice(Mobile mobile, AppiumServer appiumServer) {
        this.mobile = mobile;
        this.appiumServer = appiumServer;
        deviceTestTaskExecutor = new DeviceTestTaskExecutor(this);
    }

    public AppiumDriver freshAppiumDriver(JSONObject capabilities) {
        quitAppiumDriver();
        appiumDriver = AppiumDriverFactory.create(this, capabilities);
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
        return mobile.getId();
    }

    public boolean isConnected() {
        return mobile.getStatus() != Mobile.OFFLINE_STATUS;
    }

    public boolean isIdle() {
        return mobile.getStatus() == Mobile.IDLE_STATUS;
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
            return ServerClient.getInstance().uploadFile(screenshotFile, FileType.IMG);
        } finally {
            FileUtils.deleteQuietly(screenshotFile);
        }
    }

    public void installApp(File appFile) {
        try {
            appiumDriver.installApp(appFile.getAbsolutePath());
        } finally {
            FileUtils.deleteQuietly(appFile);
        }
    }

    public abstract void uninstallApp(String app) throws Exception;

    public Map<String, Object> dump() throws IOException, DocumentException {
        Integer type;
        String pageSource;

        if (isNativeContext()) {
            if (isAndroid()) {
                type = Page.TYPE_ANDROID_NATIVE;
            } else {
                type = Page.TYPE_IOS_NATIVE;
            }
            pageSource = XML.toJSONObject(dumpNativePage()).toString();
        } else {
            type = Page.TYPE_WEB;
            pageSource = appiumDriver.getPageSource();
        }

        return ImmutableMap.of("type", type, "pageSource", pageSource);
    }

    public abstract String dumpNativePage() throws IOException, DocumentException;

    public abstract boolean acceptAlert();

    public abstract boolean dismissAlert();

    public abstract void startRecordingScreen();

    public abstract File stopRecordingScreen() throws IOException;

    public UploadFile stopRecordingScreenAndUploadToServer() throws IOException {
        File video = stopRecordingScreen();
        try {
            return ServerClient.getInstance().uploadFile(video, FileType.VIDEO);
        } finally {
            FileUtils.deleteQuietly(video);
        }
    }

    public boolean isAndroid() {
        return mobile.getPlatform() == ANDROID;
    }
}
