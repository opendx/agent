package com.daxiang.service;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.android.AndroidDevice;
import com.daxiang.android.AndroidDeviceHolder;
import com.daxiang.android.AndroidUtils;
import com.daxiang.api.MasterApi;
import com.daxiang.exception.BusinessException;
import com.daxiang.model.Response;
import com.daxiang.utils.AndroidUtil;
import com.daxiang.utils.UUIDUtil;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class AndroidService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MasterApi masterApi;

    public Response startAdbKit(String deviceId) {
        AndroidDevice androidDevice = getAndroidDevice(deviceId);
        try {
            int port = androidDevice.getAdbKit().start();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("port", port);
            return Response.success(jsonObject);
        } catch (IOException e) {
            log.error("启动adbkit失败", e);
            return Response.fail(e.getMessage());
        }
    }

    public Response stopAdbKit(String deviceId) {
        AndroidDevice androidDevice = getAndroidDevice(deviceId);
        androidDevice.getAdbKit().stop();
        return Response.success("停止完成");
    }

    public Response freshAndroidDriver(String deviceId) {
        AndroidDevice androidDevice = getAndroidDevice(deviceId);
        AppiumDriver appiumDriver = androidDevice.freshAndroidDriver();
        JSONObject data = new JSONObject();
        data.put("appiumSessionId", appiumDriver.getSessionId().toString());
        return Response.success(data);
    }

    public Response dump(String deviceId) {
        AndroidDevice androidDevice = getAndroidDevice(deviceId);

        AppiumDriver appiumDriver = androidDevice.getAppiumDriver();
        if (appiumDriver == null) {
            return Response.fail("androidDriver为空");
        }

        String pageSource = appiumDriver.getPageSource();
        if (StringUtils.isEmpty(pageSource)) {
            return Response.fail("pageSource为空");
        }

        return Response.success("ok", XML.toJSONObject(pageSource).toString());
    }

    public Response screenshot(String deviceId) {
        AndroidDevice androidDevice = getAndroidDevice(deviceId);

        String downloadURL;
        try {
            downloadURL = screenshotByMinicapAndUploadToMaster(androidDevice);
        } catch (Exception e) {
            log.error("[{}]截图并上传到master失败", deviceId, e);
            return Response.fail(e.getMessage());
        }

        JSONObject response = new JSONObject();
        response.put("downloadURL", downloadURL);
        response.put("imgHeight", androidDevice.getDevice().getScreenHeight());
        response.put("imgWidth", androidDevice.getDevice().getScreenWidth());

        return Response.success(response);
    }

    public Response installApk(MultipartFile apk, String deviceId) {
        if (apk == null) {
            return Response.fail("apk不能为空");
        }
        if (!apk.getOriginalFilename().endsWith(".apk")) {
            return Response.fail("无法安装非APK文件");
        }

        AndroidDevice androidDevice = getAndroidDevice(deviceId);
        String apkPath = UUIDUtil.getUUID() + ".apk";
        File apkFile = new File(apkPath);
        try {
            FileUtils.copyInputStreamToFile(apk.getInputStream(), apkFile);
            AndroidUtils.installApk(androidDevice.getIDevice(), apkPath);
            return Response.success("安装成功");
        } catch (Exception e) {
            log.error("安装apk失败", e);
            return Response.fail(e.getMessage());
        } finally {
            FileUtils.deleteQuietly(apkFile);
        }
    }

    private AndroidDevice getAndroidDevice(String deviceId) {
        if (StringUtils.isEmpty(deviceId)) {
            throw new BusinessException("设备id不能为空");
        }
        AndroidDevice androidDevice = AndroidDeviceHolder.get(deviceId);
        if (androidDevice == null) {
            throw new BusinessException("设备不存在");
        }
        if (!androidDevice.isConnected()) {
            throw new BusinessException("设备未连接");
        }
        return androidDevice;
    }

    public String screenshotByMinicapAndUploadToMaster(AndroidDevice androidDevice) throws Exception {
        String screenshotFilePath = UUIDUtil.getUUID() + ".jpg";
        File screenshotFile = null;
        try {
            AndroidUtils.screenshotByMinicap(androidDevice.getIDevice(), screenshotFilePath, androidDevice.getResolution());
            screenshotFile = new File(screenshotFilePath);
            return masterApi.uploadFile(screenshotFile);
        } finally {
            FileUtils.deleteQuietly(screenshotFile);
        }
    }

    public Response aaptDumpBadging(String apkDownloadUrl) {
        if (StringUtils.isEmpty(apkDownloadUrl)) {
            return Response.fail("apk下载地址不能为空");
        }

        byte[] apkByte = restTemplate.getForObject(apkDownloadUrl, byte[].class);
        File apk = new File(UUIDUtil.getUUID() + ".apk");
        try {
            FileUtils.writeByteArrayToFile(apk, apkByte, false);
            String result = AndroidUtil.aaptDumpBadging(apk.getAbsolutePath());
            return Response.success("ok", result);
        } catch (IOException e) {
            log.error("io error", e);
            return Response.fail(e.getMessage());
        } finally {
            FileUtils.deleteQuietly(apk);
        }
    }
}
