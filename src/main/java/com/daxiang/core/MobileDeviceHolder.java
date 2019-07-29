package com.daxiang.core;

import com.android.ddmlib.IDevice;
import com.daxiang.core.android.AndroidDevice;
import com.daxiang.core.ios.IosDevice;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.MobileCapabilityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangyitao.
 */
public class MobileDeviceHolder {

    private static final Map<String, MobileDevice> MOBILE_DEVICE_HOLDER = new ConcurrentHashMap<>();

    public static void add(String deviceId, MobileDevice mobileDevice) {
        MOBILE_DEVICE_HOLDER.put(deviceId, mobileDevice);
    }

    public static MobileDevice get(String deviceId) {
        return MOBILE_DEVICE_HOLDER.get(deviceId);
    }

    public static List<MobileDevice> getAll() {
        return new ArrayList<>(MOBILE_DEVICE_HOLDER.values());
    }

    public static MobileDevice getMobileDeviceByAppiumDriver(AppiumDriver appiumDriver) {
        String deviceId = (String) appiumDriver.getCapabilities().getCapability(MobileCapabilityType.UDID);
        return get(deviceId);
    }

    public static IDevice getIDeviceByAppiumDriver(AppiumDriver appiumDriver) {
        MobileDevice mobileDevice = getMobileDeviceByAppiumDriver(appiumDriver);
        if (!(mobileDevice instanceof AndroidDevice)) {
            throw new RuntimeException("非androidDevice无法获取IDevice");
        }
        return ((AndroidDevice) mobileDevice).getIDevice();
    }

    public static AndroidDevice getAndroidDevice(String deviceId) {
        MobileDevice mobileDevice = get(deviceId);
        if (mobileDevice == null) {
            throw new RuntimeException("获取MobileDevice为空");
        }

        if (!(mobileDevice instanceof AndroidDevice)) {
            throw new RuntimeException("MobileDevice不是AndroidDevice");
        }

        return (AndroidDevice) mobileDevice;
    }

    public static IosDevice getIosDevice(String deviceId) {
        MobileDevice mobileDevice = get(deviceId);
        if (mobileDevice == null) {
            throw new RuntimeException("获取MobileDevice为空");
        }

        if (!(mobileDevice instanceof IosDevice)) {
            throw new RuntimeException("MobileDevice不是IosDevice");
        }

        return (IosDevice) mobileDevice;
    }
}