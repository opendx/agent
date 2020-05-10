package com.daxiang.service;

import com.android.ddmlib.IDevice;
import com.daxiang.core.Device;
import com.daxiang.core.DeviceHolder;
import com.daxiang.core.mobile.android.AndroidDevice;
import com.daxiang.core.mobile.android.AndroidUtil;
import com.daxiang.core.mobile.android.IDeviceExecuteShellCommandException;
import com.daxiang.model.Response;
import com.daxiang.utils.HttpUtil;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class AndroidService {

    public Response startAdbKit(String mobileId) {
        Device device = DeviceHolder.getConnectedDevice(mobileId);
        if (device == null) {
            return Response.fail(mobileId + "未连接");
        }

        try {
            int port = ((AndroidDevice) device).getAdbKit().start();
            return Response.success(ImmutableMap.of("port", port));
        } catch (IOException e) {
            log.error("[{}]启动adbkit失败", device.getId(), e);
            return Response.fail(e.getMessage());
        }
    }

    public Response stopAdbKit(String mobileId) {
        Device device = DeviceHolder.getConnectedDevice(mobileId);
        if (device == null) {
            return Response.fail(mobileId + "未连接");
        }

        ((AndroidDevice) device).getAdbKit().stop();
        return Response.success("停止完成");
    }

    public Response aaptDumpBadging(String apkDownloadUrl) {
        File apkFile = null;

        try {
            apkFile = HttpUtil.downloadFile(apkDownloadUrl);
            String result = AndroidUtil.aaptDumpBadging(apkFile.getAbsolutePath());
            return Response.success("ok", result);
        } catch (IOException e) {
            log.error("io error", e);
            return Response.fail(e.getMessage());
        } finally {
            FileUtils.deleteQuietly(apkFile);
        }
    }

    public Response getImeList(String mobileId) {
        Device device = DeviceHolder.getConnectedDevice(mobileId);
        if (device == null) {
            return Response.fail(mobileId + "未连接");
        }

        try {
            IDevice iDevice = ((AndroidDevice) device).getIDevice();
            return Response.success(AndroidUtil.getImeList(iDevice));
        } catch (IDeviceExecuteShellCommandException e) {
            log.error("[{}]获取输入法失败", device.getId(), e);
            return Response.fail(e.getMessage());
        }
    }

    public Response setIme(String mobileId, String ime) {
        Device device = DeviceHolder.getConnectedDevice(mobileId);
        if (device == null) {
            return Response.fail(mobileId + "未连接");
        }

        try {
            IDevice iDevice = ((AndroidDevice) device).getIDevice();
            AndroidUtil.setIme(iDevice, ime);
            return Response.success("设置输入法成功");
        } catch (IDeviceExecuteShellCommandException e) {
            log.error("[{}]设置输入法失败", device.getId(), e);
            return Response.fail(e.getMessage());
        }
    }
}