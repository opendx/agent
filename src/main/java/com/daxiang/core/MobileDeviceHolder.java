package com.daxiang.core;

import com.android.ddmlib.IDevice;
import com.daxiang.core.android.AndroidDevice;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import org.springframework.util.StringUtils;

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

    public static MobileDevice getConnectedDevice(String deviceId) {
        if (StringUtils.isEmpty(deviceId)) {
            return null;
        }
        MobileDevice mobileDevice = get(deviceId);
        if (mobileDevice == null || !mobileDevice.isConnected()) {
            return null;
        }
        return mobileDevice;
    }

    public static MobileDevice getIdleDevice(String deviceId) {
        MobileDevice mobileDevice = getConnectedDevice(deviceId);
        if (mobileDevice != null && mobileDevice.isIdle()) {
            return mobileDevice;
        }
        return null;
    }
}