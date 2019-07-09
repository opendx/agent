package com.daxiang.android;

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

    public static List<AndroidDevice> getAll() {
        return new ArrayList<>(androidDeviceHolder.values());
    }
}
