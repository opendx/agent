package com.daxiang.core.ios;

import com.daxiang.core.appium.AppiumServer;
import com.daxiang.model.Device;
import io.appium.java_client.AppiumDriver;

/**
 * Created by jiangyitao.
 */
public class IosDevice {

    private Device device;

    private AppiumServer appiumServer;
    private AppiumDriver appiumDriver;

    public IosDevice(Device device) {
        this.device = device;
    }
}
