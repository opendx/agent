package com.daxiang.service;

import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.android.AndroidDevice;
import com.daxiang.core.android.AndroidUtil;
import com.daxiang.model.Response;
import com.daxiang.utils.UUIDUtil;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

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

        // download apk
        byte[] apkByteArr = restTemplate.getForObject(apkDownloadUrl, byte[].class);
        File apkFile = new File(UUIDUtil.getUUID() + ".apk");
        try {
            FileUtils.writeByteArrayToFile(apkFile, apkByteArr, false);
            String result = AndroidUtil.aaptDumpBadging(apkFile.getAbsolutePath());
            return Response.success("ok", result);
        } catch (IOException e) {
            log.error("io error", e);
            return Response.fail(e.getMessage());
        } finally {
            FileUtils.deleteQuietly(apkFile);
        }
    }

    public Response getImeList(String deviceId) {
        MobileDevice device = MobileDeviceHolder.getConnectedDevice(deviceId);
        if (device == null) {
            return Response.fail("设备未连接");
        }

        try {
            return Response.success(AndroidUtil.getImeList(((AndroidDevice) device).getIDevice()));
        } catch (Exception e) {
            log.error("[{}]获取输入法列表异常", device.getId(), e);
            return Response.fail(e.getMessage());
        }
    }

    public Response setIme(String deviceId, String ime) {
        MobileDevice device = MobileDeviceHolder.getConnectedDevice(deviceId);
        if (device == null) {
            return Response.fail("设备未连接");
        }

        try {
            AndroidUtil.setIme(((AndroidDevice) device).getIDevice(), ime);
            return Response.success("设置成功");
        } catch (Exception e) {
            log.error("[{}]设置输入法异常", device.getId(), e);
            return Response.fail(e.getMessage());
        }
    }
}