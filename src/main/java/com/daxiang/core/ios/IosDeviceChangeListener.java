package com.daxiang.core.ios;

/**
 * Created by jiangyitao.
 */
public interface IosDeviceChangeListener {
    void onDeviceConnected(String deviceId);
    void onDeviceDisconnected(String deviceId);
}
