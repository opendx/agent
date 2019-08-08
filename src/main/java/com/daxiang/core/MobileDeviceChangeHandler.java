package com.daxiang.core;

import com.daxiang.api.MasterApi;
import com.daxiang.model.Device;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by jiangyitao.
 */
public class MobileDeviceChangeHandler {

    @Autowired
    private MasterApi masterApi;

    public Device getDeviceById(String deviceId) {
        return masterApi.getDeviceById(deviceId);
    }

    public void mobileOnline(MobileDevice mobileDevice) {
        mobileDevice.saveOnlineDeviceToMaster();
    }

    public void mobileOffline(String deviceId) {
        MobileDevice mobileDevice = MobileDeviceHolder.get(deviceId);
        if (mobileDevice == null) {
            return;
        }
        mobileDevice.saveOfflineDeviceToMaster();
    }
}