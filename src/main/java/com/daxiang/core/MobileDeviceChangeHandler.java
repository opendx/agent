package com.daxiang.core;

import com.daxiang.api.MasterApi;
import com.daxiang.model.Device;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

/**
 * Created by jiangyitao.
 */
public class MobileDeviceChangeHandler {

    @Autowired
    private MasterApi masterApi;
    @Value("${server.address}")
    private String ip;
    @Value("${server.port}")
    private Integer port;

    public Device getDeviceById(String deviceId) {
        return masterApi.getDeviceById(deviceId);
    }

    public void mobileOnline(MobileDevice mobileDevice) {
        Device device = mobileDevice.getDevice();
        device.setAgentIp(ip);
        device.setAgentPort(port);
        device.setStatus(Device.IDLE_STATUS);
        device.setLastOnlineTime(new Date());
        masterApi.saveDevice(device);
    }

    public void mobileDisconnected(String deviceId) {
        MobileDevice mobileDevice = MobileDeviceHolder.get(deviceId);
        if (mobileDevice == null) {
            return;
        }
        Device device = mobileDevice.getDevice();
        device.setStatus(Device.OFFLINE_STATUS);
        device.setLastOfflineTime(new Date());
        masterApi.saveDevice(device);
    }
}