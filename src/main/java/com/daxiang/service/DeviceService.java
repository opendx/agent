package com.daxiang.service;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.core.Device;
import com.daxiang.core.DeviceHolder;
import com.daxiang.core.mobile.MobileDevice;
import com.daxiang.model.Response;
import com.daxiang.model.UploadFile;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Dimension;
import org.springframework.stereotype.Service;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class DeviceService {

    public Response screenshot(String deviceId) {
        Device device = DeviceHolder.getConnectedDevice(deviceId);
        if (device == null) {
            return Response.fail(deviceId + "未连接");
        }

        UploadFile uploadFile = device.screenshotThenUploadToServer();

        JSONObject response = new JSONObject();
        response.put("imgUrl", uploadFile.getDownloadUrl());
        response.put("imgPath", uploadFile.getFilePath());

        if (device instanceof MobileDevice) {
            MobileDevice mobileDevice = (MobileDevice) device;
            if (mobileDevice.isNativeContext()) {
                // 由于ios截图分辨率与dump的windowHierarchy bounds不一致，需要把当前屏幕信息传给前端处理
                // 在竖/横屏时，若android截图有虚拟按键，这里window的高度/宽度不包含虚拟按键
                AppiumDriver driver = (AppiumDriver) mobileDevice.getDriver();
                Dimension window = driver.manage().window().getSize();
                response.put("windowHeight", window.getHeight());
                response.put("windowWidth", window.getWidth());
                response.put("windowOrientation", driver.getOrientation().value());
            }
        }

        return Response.success(response);
    }

    public Response dump(String deviceId) {
        Device device = DeviceHolder.getConnectedDevice(deviceId);
        if (device == null) {
            return Response.fail(deviceId + "未连接");
        }

        return Response.success(device.dump());
    }
}
