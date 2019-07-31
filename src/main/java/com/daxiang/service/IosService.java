package com.daxiang.service;

import com.daxiang.api.MasterApi;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.ios.IosUtil;
import com.daxiang.model.Response;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Created by jiangyitao.
 */
@Service
public class IosService {

    @Autowired
    private MasterApi masterApi;

    public String screenshotByIdeviceScreenshotAndUploadToMaster(String deviceId) throws Exception {
        File screenshotFile = null;
        try {
            screenshotFile = IosUtil.screenshotByIdeviceScreenshot(deviceId);
            return masterApi.uploadFile(screenshotFile);
        } finally {
            FileUtils.deleteQuietly(screenshotFile);
        }
    }

    public Response pressHome(String deviceId) {
        MobileDevice mobileDevice = MobileDeviceHolder.get(deviceId);
        if (mobileDevice == null) {
            return Response.fail("设备未连接");
        }
        if (mobileDevice.getAppiumDriver() == null) {
            return Response.fail("AppiumDriver为空");
        }
        IosUtil.pressHome(mobileDevice.getAppiumDriver());
        return Response.success("执行成功");
    }
}
