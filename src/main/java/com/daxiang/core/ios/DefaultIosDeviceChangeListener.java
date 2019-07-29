package com.daxiang.core.ios;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
public class DefaultIosDeviceChangeListener implements IosDeviceChangeListener {

    @Override
    public void onDeviceConnected(String deviceId) {
        new Thread(() -> iosDeviceConnected(deviceId)).start();
    }

    @Override
    public void onDeviceDisConnected(String deviceId) {
        new Thread(() -> iosDeviceDisconnected(deviceId)).start();
    }

    private void iosDeviceConnected(String deviceId) {
    }

    private void iosDeviceDisconnected(String deviceId) {

    }
}