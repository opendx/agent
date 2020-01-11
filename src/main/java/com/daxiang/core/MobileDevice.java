package com.daxiang.core;

import com.daxiang.App;
import com.daxiang.api.MasterApi;
import com.daxiang.core.appium.AppiumDriverFactory;
import com.daxiang.core.appium.AppiumServer;
import com.daxiang.model.Device;
import com.daxiang.model.FileType;
import com.daxiang.utils.UUIDUtil;
import io.appium.java_client.AppiumDriver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Data
public abstract class MobileDevice {

    public static final String NATIVE_CONTEXT = "NATIVE_APP";

    public static final int ANDROID = 1;
    public static final int IOS = 2;

    /* 0: 竖直 */
    private Integer orientation = 0;
    /* 分辨率 eg.1080x1920 */
    private String resolution;

    private Device device;
    private DeviceTestTaskExecutor deviceTestTaskExecutor;

    private AppiumServer appiumServer;
    private AppiumDriver appiumDriver;

    public MobileDevice(Device device, AppiumServer appiumServer) {
        this.device = device;
        this.resolution = device.getScreenWidth() + "x" + device.getScreenHeight();
        this.appiumServer = appiumServer;
        deviceTestTaskExecutor = new DeviceTestTaskExecutor(this);
    }

    /**
     * 刷新AppiumDriver
     *
     * @return
     */
    public AppiumDriver freshAppiumDriver(Integer platform) {
        quitAppiumDriver();
        appiumDriver = AppiumDriverFactory.create(this, platform);
        return appiumDriver;
    }

    public void quitAppiumDriver() {
        if (appiumDriver != null) {
            // 退出上次的会话
            try {
                appiumDriver.quit();
            } catch (Exception e) {
                // 上次会话可能已经过期，quit会有异常，ignore
            }
        }
    }

    public abstract AppiumDriver initAppiumDriver();

    public String getId() {
        return device.getId();
    }

    /**
     * 设备是否连接
     *
     * @return
     */
    public boolean isConnected() {
        return device.getStatus() != Device.OFFLINE_STATUS;
    }

    /**
     * 是否在线闲置
     *
     * @return
     */
    public boolean isIdle() {
        return device.getStatus() == Device.IDLE_STATUS;
    }

    public String getVirtualResolution(int width) {
        int height = getScreenScaledHeight(width);
        return width + "x" + height;
    }

    /**
     * 按照比例计算高度
     *
     * @param width
     * @return
     */
    public int getScreenScaledHeight(int width) {
        int screenHeight = getDevice().getScreenHeight();
        int screenWidth = getDevice().getScreenWidth();
        float scale = screenHeight / (float) screenWidth;
        return (int) (scale * width);
    }

    public abstract File screenshot() throws IOException;

    public String screenshotAndUploadToMaster() throws IOException {
        File screenshotFile = screenshot();
        try {
            return MasterApi.getInstance().uploadFile(screenshotFile, FileType.IMG);
        } finally {
            FileUtils.deleteQuietly(screenshotFile);
        }
    }

    public abstract void installApp(File appFile) throws Exception;

    public void installApp(MultipartFile app) throws Exception {
        File appFile = new File(UUIDUtil.getUUID() + "." + StringUtils.unqualify(app.getOriginalFilename()));
        try {
            FileUtils.copyInputStreamToFile(app.getInputStream(), appFile);
            installApp(appFile);
        } finally {
            FileUtils.deleteQuietly(appFile);
        }
    }

    public abstract String dump() throws IOException, DocumentException;

    public void saveOnlineDeviceToMaster() {
        device.setAgentIp(App.getProperty("server.address"));
        device.setAgentPort(Integer.parseInt(App.getProperty("server.port")));
        device.setStatus(Device.IDLE_STATUS);
        device.setLastOnlineTime(new Date());
        log.info("saveOnlineDeviceToMaster: {}", device);
        MasterApi.getInstance().saveDevice(device);
    }

    public void saveUsingDeviceToMaster(String username) {
        if (isConnected()) {
            device.setStatus(Device.USING_STATUS);
            device.setUsername(username);
            log.info("saveUsingDeviceToMaster: {}", device);
            MasterApi.getInstance().saveDevice(device);
        }
    }

    public void saveIdleDeviceToMaster() {
        if (isConnected()) {
            device.setStatus(Device.IDLE_STATUS);
            log.info("saveIdleDeviceToMaster: {}", device);
            MasterApi.getInstance().saveDevice(device);
        }
    }

    public void saveOfflineDeviceToMaster() {
        device.setStatus(Device.OFFLINE_STATUS);
        device.setLastOfflineTime(new Date());
        log.info("saveOfflineDeviceToMaster: {}", device);
        MasterApi.getInstance().saveDevice(device);
    }

    /**
     * 当前是否是原生context
     *
     * @return
     */
    public boolean isNativeContext() {
        if (appiumDriver == null) {
            throw new RuntimeException("appiumDriver未初始化");
        }
        return NATIVE_CONTEXT.equals(appiumDriver.getContext());
    }

    public abstract void startRecordingScreen();

    public abstract File stopRecordingScreen() throws IOException;

    public String stopRecordingScreenAndUploadToMaster() throws IOException {
        File video = null;
        try {
            video = stopRecordingScreen();
            return MasterApi.getInstance().uploadFile(video, FileType.VIDEO);
        } finally {
            FileUtils.deleteQuietly(video);
        }
    }

    public abstract void installApp(String appDownloadUrl) throws Exception;

    public File downloadApp(String appDownloadUrl) throws IOException {
        if (StringUtils.isEmpty(appDownloadUrl)) {
            throw new IllegalArgumentException("appDownloadUrl cannot be empty");
        }

        // download app
        RestTemplate restTemplate = App.getBean(RestTemplate.class);
        byte[] appBytes = restTemplate.getForObject(appDownloadUrl, byte[].class);

        File app = new File(UUIDUtil.getUUID() + "." + StringUtils.unqualify(appDownloadUrl));
        FileUtils.writeByteArrayToFile(app, appBytes, false);
        return app;
    }
}