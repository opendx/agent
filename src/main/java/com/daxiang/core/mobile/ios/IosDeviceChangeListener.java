package com.daxiang.core.mobile.ios;

import com.android.ddmlib.IDevice;

/**
 * Created by jiangyitao.
 */
public interface IosDeviceChangeListener {
    void deviceConnected(IDevice iDevice);
    void deviceDisconnected(IDevice iDevice);
}
