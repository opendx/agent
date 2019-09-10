package com.daxiang.action.appium.android;

import com.daxiang.App;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.android.AndroidUtil;
import com.daxiang.utils.UUIDUtil;
import io.appium.java_client.AppiumDriver;
import org.apache.commons.io.FileUtils;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.io.File;

/**
 * Created by jiangyitao.
 */
public class InstallApk {

    private AppiumDriver driver;

    public InstallApk(AppiumDriver driver) {
        this.driver = driver;
    }

    public void execute(Object apkDownloadUrl) throws Exception {
        Assert.notNull(apkDownloadUrl, "apk下载地址不能为空");
        String _apkDownloadUrl = (String) apkDownloadUrl;

        // download apk
        RestTemplate restTemplate = App.getBean(RestTemplate.class);
        byte[] apkBytes = restTemplate.getForObject(_apkDownloadUrl, byte[].class);

        File apk = new File(UUIDUtil.getUUID() + ".apk");
        try {
            FileUtils.writeByteArrayToFile(apk, apkBytes, false);
            // install apk
            AndroidUtil.installApk(MobileDeviceHolder.getIDeviceByAppiumDriver(driver), apk.getAbsolutePath());
        } finally {
            // delete apk
            FileUtils.deleteQuietly(apk);
        }
    }
}
