package com.daxiang.core.ios;

import com.daxiang.core.MobileDevice;
import com.daxiang.model.Device;

/**
 * Created by jiangyitao.
 */
public class IosDevice extends MobileDevice {

    public IosDevice(Device device) {
        super(device);
    }

    public int getMjpegServerPort() {
        Object mjpegServerPort = getAppiumDriver().getCapabilities().asMap().get("mjpegServerPort");
        return (int)((long) mjpegServerPort);
    }
}
