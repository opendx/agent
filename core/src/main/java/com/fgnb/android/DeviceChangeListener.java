package com.fgnb.android;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.fgnb.service.AndroidDeviceChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by jiangyitao.
 * 安卓手机接入，拔出监听器
 */
@Component
@Slf4j
public class DeviceChangeListener implements AndroidDebugBridge.IDeviceChangeListener {

    @Autowired
    private AndroidDeviceChangeService androidDeviceChangeService;

    @Override
    public void deviceConnected(IDevice device) {
        //让多个手机可以同时接入，这里开新的线程处理
        new Thread(() -> androidDeviceChangeService.deviceConnected(device)).start();
    }

    @Override
    public void deviceDisconnected(IDevice device) {
        //让多个手机可以同时断开连接，这里开新的线程处理
        new Thread(()-> androidDeviceChangeService.deviceDisconnected(device)).start();
    }

    @Override
    public void deviceChanged(IDevice device, int changeMask) {
        //ignore
    }
}
