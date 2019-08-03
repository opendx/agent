package com.daxiang.service;

import com.daxiang.api.MasterApi;
import com.daxiang.core.ios.IosUtil;
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
}
