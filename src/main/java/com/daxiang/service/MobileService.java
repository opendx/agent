package com.daxiang.service;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.openqa.selenium.Dimension;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class MobileService {

    public Response screenshot(String deviceId) {
        MobileDevice mobileDevice = MobileDeviceHolder.getConnectedDevice(deviceId);
        if (mobileDevice == null) {
            return Response.fail("设备未连接");
        }

        String downloadURL;
        try {
            downloadURL = mobileDevice.screenshotAndUploadToMaster();
        } catch (Exception e) {
            log.error("[{}]截图并上传到master失败", deviceId, e);
            return Response.fail(e.getMessage());
        }

        JSONObject response = new JSONObject();
        response.put("downloadURL", downloadURL);

        Dimension window = mobileDevice.getAppiumDriver().manage().window().getSize();
        response.put("imgHeight", window.getHeight());
        response.put("imgWidth", window.getWidth());

        return Response.success(response);
    }

    public Response dump(String deviceId) {
        MobileDevice mobileDevice = MobileDeviceHolder.getConnectedDevice(deviceId);
        if (mobileDevice == null) {
            return Response.fail("设备未连接");
        }
        if (!mobileDevice.isNativeContext()) {
            return Response.success("ok", mobileDevice.getAppiumDriver().getPageSource());
        }

        try {
            String pageSource = mobileDevice.dump();
            return Response.success("ok", pageSource);
        } catch (DocumentException e) {
            log.error("读取pageSource出错", e);
            return Response.fail("读取pageSource出错，请稍后重试");
        } catch (IOException e) {
            log.error("io err", e);
            return Response.fail(e.getMessage());
        }
    }

    public Response installApp(MultipartFile app, String deviceId) {
        MobileDevice mobileDevice = MobileDeviceHolder.getConnectedDevice(deviceId);
        if (mobileDevice == null) {
            return Response.fail("设备未连接");
        }
        try {
            mobileDevice.installApp(app);
            return Response.success("安装成功");
        } catch (Exception e) {
            log.error("安装app失败", e);
            return Response.fail(e.getMessage());
        }
    }

}