package com.daxiang.service;

import com.daxiang.core.Device;
import com.daxiang.core.DeviceHolder;
import com.daxiang.core.mobile.MobileDevice;
import com.daxiang.exception.AgentException;
import com.daxiang.model.FileType;
import com.daxiang.model.UploadFile;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Dimension;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class DeviceService {

    public Map<String, Object> screenshot(String deviceId) {
        Device device = DeviceHolder.getConnectedDevice(deviceId);
        if (device == null) {
            throw new AgentException(deviceId + "未连接");
        }

        UploadFile uploadFile = device.screenshotAndUploadToServer(FileType.TMP);

        Map<String, Object> res = new HashMap<>();
        res.put("imgUrl", uploadFile.getDownloadUrl());
        res.put("imgPath", uploadFile.getFilePath());

        if (device instanceof MobileDevice) {
            MobileDevice mobileDevice = (MobileDevice) device;
            if (mobileDevice.isNativeContext()) {
                // 由于ios截图分辨率与dump的windowHierarchy bounds不一致，需要把当前屏幕信息传给前端处理
                // 在竖/横屏时，若android截图有虚拟按键，这里window的高度/宽度不包含虚拟按键
                AppiumDriver driver = (AppiumDriver) mobileDevice.getDriver();
                Dimension window = driver.manage().window().getSize();
                res.put("windowHeight", window.getHeight());
                res.put("windowWidth", window.getWidth());
                res.put("windowOrientation", driver.getOrientation().value());
            }
        }

        return res;
    }

    public Map<String, Object> dump(String deviceId) {
        Device device = DeviceHolder.getConnectedDevice(deviceId);
        if (device == null) {
            throw new AgentException(deviceId + "未连接");
        }

        return device.dump();
    }
}
