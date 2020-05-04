package com.daxiang.core.ios;

/**
 * Created by jiangyitao.
 */
public interface IosDeviceChangeListener {
    void onDeviceConnected(String deviceId, boolean isRealDevice);
    void onDeviceDisconnected(String deviceId, boolean isRealDevice);
}
