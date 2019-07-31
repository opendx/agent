package com.daxiang.service;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.android.AndroidDevice;
import com.daxiang.core.appium.AndroidNativePageSourceConverter;
import com.daxiang.model.Response;
import com.google.common.collect.ImmutableMap;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class MobileService {

    @Autowired
    private AndroidService androidService;
    @Autowired
    private IosService iosService;

    public Response screenshot(String deviceId) {
        MobileDevice mobileDevice = MobileDeviceHolder.getConnectedDevice(deviceId);
        if (mobileDevice == null) {
            return Response.fail("设备未连接");
        }

        String downloadURL;
        try {
            if (mobileDevice instanceof AndroidDevice) {
                downloadURL = androidService.screenshotByMinicapAndUploadToMaster((AndroidDevice) mobileDevice);
            } else {
                downloadURL = iosService.screenshotByIdeviceScreenshotAndUploadToMaster(deviceId);
            }
        } catch (Exception e) {
            log.error("[{}]截图并上传到master失败", deviceId, e);
            return Response.fail(e.getMessage());
        }

        JSONObject response = new JSONObject();
        response.put("downloadURL", downloadURL);
        response.put("imgHeight", mobileDevice.getDevice().getScreenHeight());
        response.put("imgWidth", mobileDevice.getDevice().getScreenWidth());

        return Response.success(response);
    }

    public Response dump(String deviceId) {
        MobileDevice mobileDevice = MobileDeviceHolder.getConnectedDevice(deviceId);
        if (mobileDevice == null) {
            return Response.fail("设备未连接");
        }
        // todo ios
        // 由于appium pageSource返回的xml不是规范的xml，需要把除了hierarchy节点以外的节点替换成node，否则xml转json会出问题
        try {
            String pageSource = AndroidNativePageSourceConverter.convert(mobileDevice.getAppiumDriver().getPageSource());
            return Response.success("ok", pageSource);
        } catch (DocumentException e) {
            log.error("读取pageSource出错", e);
            return Response.fail("读取pageSource出错，请稍后重试");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    public Response freshDriver(String deviceId) {
        MobileDevice mobileDevice = MobileDeviceHolder.getConnectedDevice(deviceId);
        if (mobileDevice == null) {
            return Response.fail("设备未连接");
        }

        AppiumDriver appiumDriver = mobileDevice.freshDriver();
        return Response.success(ImmutableMap.of("appiumSessionId", appiumDriver.getSessionId().toString()));
    }

    public Response installApp(MultipartFile app, String deviceId) {
        MobileDevice mobileDevice = MobileDeviceHolder.getConnectedDevice(deviceId);
        if (mobileDevice == null) {
            return Response.fail("设备未连接");
        }
        // todo android and ios

    }

}