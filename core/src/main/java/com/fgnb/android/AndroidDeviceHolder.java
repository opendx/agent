package com.fgnb.android;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangyitao.
 */
public class AndroidDeviceHolder {

    private static Map<String,AndroidDevice> androidDeviceHolder = new ConcurrentHashMap<>();

    public static void addAndroidDevice(String deviceId,AndroidDevice androidDevice){
        androidDeviceHolder.put(deviceId,androidDevice);
    }

    public static AndroidDevice getAndroidDevice(String deviceId){
        return androidDeviceHolder.get(deviceId);
    }

    public static void removeIDevice(String deviceId){
        androidDeviceHolder.remove(deviceId);
    }
}
