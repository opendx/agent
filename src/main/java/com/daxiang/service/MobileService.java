package com.daxiang.service;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.App;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.android.AndroidDevice;
import com.daxiang.core.appium.AppiumPageSourceConverter;
import com.daxiang.core.ios.IosDevice;
import com.daxiang.model.Response;
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

        try {
            String pageSource = AppiumPageSourceConverter.getJSONStringPageSource(mobileDevice.getAppiumDriver());
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

        JSONObject data = new JSONObject();
        data.put("appiumSessionId", appiumDriver.getSessionId().toString());

        if (mobileDevice instanceof IosDevice) {
            data.put("mjpegServerPort", ((IosDevice) mobileDevice).getMjpegServerPort());

            int displayWidth = Integer.parseInt(App.getProperty("displayWidth"));
            int displayHeight = mobileDevice.getScreenScaledHeight(displayWidth);

            data.put("displayWidth", displayWidth);
            data.put("displayHeight", displayHeight);
        }

        return Response.success(data);
    }

    public Response installApp(MultipartFile app, String deviceId) {
        MobileDevice mobileDevice = MobileDeviceHolder.getConnectedDevice(deviceId);
        if (mobileDevice == null) {
            return Response.fail("设备未连接");
        }

        try {
            if (mobileDevice instanceof AndroidDevice) {
                androidService.installApk(app, ((AndroidDevice) mobileDevice).getIDevice());
            } else {
                iosService.installIpa(app, deviceId);
            }
            return Response.success("安装成功");
        } catch (Exception e) {
            log.error("安装app失败", e);
            return Response.fail(e.getMessage());
        }
    }

}