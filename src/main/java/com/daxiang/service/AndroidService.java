package com.daxiang.service;

import com.android.ddmlib.IDevice;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.android.AndroidDevice;
import com.daxiang.core.android.AndroidUtil;
import com.daxiang.api.MasterApi;
import com.daxiang.model.Response;
import com.daxiang.utils.UUIDUtil;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
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
        MobileDevice mobileDevice = MobileDeviceHolder.getConnectedDevice(deviceId);
        if (mobileDevice == null) {
            return Response.fail("设备未连接");
        }

        try {
            int port = ((AndroidDevice) mobileDevice).getAdbKit().start();
            return Response.success(ImmutableMap.of("port", port));
        } catch (IOException e) {
            log.error("启动adbkit失败", e);
            return Response.fail(e.getMessage());
        }
    }

    public Response stopAdbKit(String deviceId) {
        MobileDevice mobileDevice = MobileDeviceHolder.getConnectedDevice(deviceId);
        if (mobileDevice == null) {
            return Response.fail("设备未连接");
        }

        ((AndroidDevice) mobileDevice).getAdbKit().stop();
        return Response.success("停止完成");
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

    public void installApk(MultipartFile apk, IDevice iDevice) throws Exception {
        String apkPath = UUIDUtil.getUUID() + ".apk";
        File apkFile = new File(apkPath);
        try {
            FileUtils.copyInputStreamToFile(apk.getInputStream(), apkFile);
            AndroidUtil.installApk(iDevice, apkPath);
        } finally {
            FileUtils.deleteQuietly(apkFile);
        }
    }

    public String screenshotByMinicapAndUploadToMaster(AndroidDevice androidDevice) throws Exception {
        return screenshotByMinicapAndUploadToMaster(androidDevice.getIDevice(), androidDevice.getResolution());
    }

    public String screenshotByMinicapAndUploadToMaster(IDevice iDevice, String resolution) throws Exception {
        String screenshotFilePath = UUIDUtil.getUUID() + ".jpg";
        File screenshotFile = null;
        try {
            AndroidUtil.screenshotByMinicap(iDevice, screenshotFilePath, resolution);
            screenshotFile = new File(screenshotFilePath);
            return masterApi.uploadFile(screenshotFile);
        } finally {
            FileUtils.deleteQuietly(screenshotFile);
        }
    }

}