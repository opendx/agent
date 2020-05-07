package com.daxiang.core.mobile.ios;

/**
 * Created by jiangyitao.
 */
public interface IosDeviceChangeListener {
    void onDeviceConnected(String deviceId, boolean isRealDevice);
    void onDeviceDisconnected(String deviceId, boolean isRealDevice);
}
