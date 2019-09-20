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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
public class InstallApk {

    private AppiumDriver driver;
    private ScheduledExecutorService service;

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
            handleInstallBtnAsync();
            // install apk
            AndroidUtil.installApk(MobileDeviceHolder.getIDeviceByAppiumDriver(driver), apk.getAbsolutePath());
            stopHandleInstallBtn();
        } finally {
            // delete apk
            FileUtils.deleteQuietly(apk);
        }
    }

    /**
     * 处理安装app时弹窗
     */
    private void handleInstallBtnAsync() {
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            try {
                driver.findElementByXPath("//android.widget.Button[contains(@text,'安装')]").click();
                service.shutdown();
            } catch (Exception ignore) {
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * 安装完成后停止处理安装弹窗
     */
    private void stopHandleInstallBtn() {
        service.shutdown();
    }
}
