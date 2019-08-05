package com.daxiang.service;

import com.daxiang.api.MasterApi;
import com.daxiang.core.ios.IosUtil;
import com.daxiang.utils.UUIDUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

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

    public void installIpa(MultipartFile ipa, String deviceId) throws IOException {
        String ipaPath = UUIDUtil.getUUID() + ".ipa";
        File ipaFile = new File(ipaPath);
        try {
            FileUtils.copyInputStreamToFile(ipa.getInputStream(), ipaFile);
            IosUtil.installIpa(ipaFile.getAbsolutePath(), deviceId);
        } finally {
            FileUtils.deleteQuietly(ipaFile);
        }
    }
}
