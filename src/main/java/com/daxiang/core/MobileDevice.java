package com.daxiang.core;

import com.daxiang.core.appium.AppiumDriverFactory;
import com.daxiang.core.appium.AppiumServer;
import com.daxiang.model.Device;
import io.appium.java_client.AppiumDriver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Data
public class MobileDevice {

    public static final int ANDROID = 1;
    public static final int IOS = 2;

    private Device device;
    private DeviceTestTaskExcutor deviceTestTaskExcutor;

    private AppiumServer appiumServer;
    private AppiumDriver appiumDriver;

    public MobileDevice(Device device) {
        this.device = device;
        deviceTestTaskExcutor = new DeviceTestTaskExcutor(this);
    }

    public String getId() {
        return device.getId();
    }

    /**
     * 刷新AppiumDriver
     *
     * @return
     */
    public AppiumDriver freshDriver() {
        if (appiumDriver != null) {
            // 退出上次的会话
            try {
                appiumDriver.quit();
            } catch (Exception e) {
                // 上次会话可能已经过期，quit会有异常，ignore
            }
        }
        appiumDriver = AppiumDriverFactory.create(this, appiumServer.getUrl());
        return appiumDriver;
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
     * 获取设备屏幕分辨率
     *
     * @return eg.1080x1920
     */
    public String getResolution() {
        return String.valueOf(device.getScreenWidth()) + "x" + String.valueOf(device.getScreenHeight());
    }

}