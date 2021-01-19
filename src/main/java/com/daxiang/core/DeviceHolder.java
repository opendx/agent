package com.daxiang.core;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangyitao.
 */
public class DeviceHolder {

    private static final Map<String, Device> DEVICE_HOLDER = new ConcurrentHashMap<>();

    public static void put(String deviceId, Device device) {
        DEVICE_HOLDER.put(deviceId, device);
    }

    public static Device remove(String deviceId) {
        return DEVICE_HOLDER.remove(deviceId);
    }

    public static Device get(String deviceId) {
        return DEVICE_HOLDER.get(deviceId);
    }

    public static List<Device> getAll() {
        return new ArrayList<>(DEVICE_HOLDER.values());
    }

    public static Device getConnectedDevice(String deviceId) {
        if (!StringUtils.isEmpty(deviceId)) {
            Device device = get(deviceId);
            if (device != null && device.isConnected()) {
                return device;
            }
        }

        return null;
    }

    public static Device getIdleDevice(String deviceId) {
        if (!StringUtils.isEmpty(deviceId)) {
            Device device = get(deviceId);
            if (device != null && device.isIdle()) {
                return device;
            }
        }

        return null;
    }
}