package com.daxiang.service;

import com.android.ddmlib.IDevice;
import com.daxiang.core.Device;
import com.daxiang.core.DeviceHolder;
import com.daxiang.core.mobile.android.AndroidDevice;
import com.daxiang.core.mobile.android.AndroidUtil;
import com.daxiang.core.mobile.android.IDeviceExecuteShellCommandException;
import com.daxiang.exception.AgentException;
import com.daxiang.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class AndroidService {

    public int startAdbKit(String mobileId) {
        Device device = DeviceHolder.getConnectedDevice(mobileId);
        if (device == null) {
            throw new AgentException(mobileId + "未连接");
        }

        try {
            return ((AndroidDevice) device).getAdbKit().start();
        } catch (IOException e) {
            log.error("[{}]启动adbkit失败", mobileId, e);
            throw new AgentException(e.getMessage());
        }
    }

    public void stopAdbKit(String mobileId) {
        Device device = DeviceHolder.getConnectedDevice(mobileId);
        if (device == null) {
            throw new AgentException(mobileId + "未连接");
        }

        ((AndroidDevice) device).getAdbKit().stop();
    }

    public String aaptDumpBadging(String apkDownloadUrl) {
        File apkFile = new File(UUIDUtil.getUUID());
        try {
            FileUtils.copyURLToFile(new URL(apkDownloadUrl), apkFile);
            return AndroidUtil.aaptDumpBadging(apkFile.getAbsolutePath());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new AgentException(e.getMessage());
        } finally {
            FileUtils.deleteQuietly(apkFile);
        }
    }

    public List<String> getImeList(String mobileId) {
        Device device = DeviceHolder.getConnectedDevice(mobileId);
        if (device == null) {
            throw new AgentException(mobileId + "未连接");
        }

        try {
            IDevice iDevice = ((AndroidDevice) device).getIDevice();
            return AndroidUtil.getImeList(iDevice);
        } catch (IDeviceExecuteShellCommandException e) {
            log.error("[{}]获取输入法失败", mobileId, e);
            throw new AgentException(e.getMessage());
        }
    }

    public void setIme(String mobileId, String ime) {
        Device device = DeviceHolder.getConnectedDevice(mobileId);
        if (device == null) {
            throw new AgentException(mobileId + "未连接");
        }

        try {
            IDevice iDevice = ((AndroidDevice) device).getIDevice();
            AndroidUtil.setIme(iDevice, ime);
        } catch (IDeviceExecuteShellCommandException e) {
            log.error("[{}]设置输入法失败", mobileId, e);
            throw new AgentException(e.getMessage());
        }
    }
}
