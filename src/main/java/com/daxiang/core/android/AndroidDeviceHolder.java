package com.daxiang.core.android;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.MobileCapabilityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangyitao.
 */
public class AndroidDeviceHolder {

    private static Map<String, AndroidDevice> androidDeviceHolder = new ConcurrentHashMap<>();

    public static void add(String deviceId, AndroidDevice androidDevice) {
        androidDeviceHolder.put(deviceId, androidDevice);
    }

    public static AndroidDevice get(String deviceId) {
        return androidDeviceHolder.get(deviceId);
    }

    public static AndroidDevice get(AppiumDriver appiumDriver) {
        String deviceId = (String) appiumDriver.getCapabilities().getCapability(MobileCapabilityType.UDID);
        return get(deviceId);
    }

    public static List<AndroidDevice> getAll() {
        return new ArrayList<>(androidDeviceHolder.values());
    }
}
