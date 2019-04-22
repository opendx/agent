package com.fgnb.android;

import com.android.ddmlib.*;
import com.fgnb.App;
import com.fgnb.android.stf.StfResourceReleaser;
import com.fgnb.android.stf.minicap.MinicapInstaller;
import com.fgnb.android.stf.minitouch.MinitouchInstaller;
import com.fgnb.android.uiautomator.Uiautomator2ServerApkInstaller;
import com.fgnb.api.ServerApi;
import com.fgnb.model.Device;
import com.fgnb.utils.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;

/**
 * Created by jiangyitao.
 * 安卓手机接入，拔出监听器
 */
@Component
@Slf4j
public class AndroidDeviceChangeListener implements AndroidDebugBridge.IDeviceChangeListener {

    @Autowired
    private ServerApi serverApi;

    @Override
    public void deviceConnected(IDevice device) {
        new Thread(() -> {
            try {
                androidDeviceConnected(device);
            } catch (Exception e) {
                log.error("[{}]设备连接处理出错", device.getSerialNumber(), e);
            }
        }).start();
    }

    @Override
    public void deviceDisconnected(IDevice device) {
        new Thread(() -> androidDeviceDisconnected(device)).start();
    }

    @Override
    public void deviceChanged(IDevice device, int changeMask) {
        //ignore
    }

    /**
     * Android设备连接到电脑，调用的方法
     *
     * @param iDevice
     * @throws Exception
     */
    private void androidDeviceConnected(IDevice iDevice) throws Exception {
        String deviceId = iDevice.getSerialNumber();
        log.info("[{}]已连接", deviceId);

        log.info("[{}]等待手机上线", deviceId);
        AndroidUtils.waitForDeviceOnline(iDevice, 60);
        log.info("[{}]手机已上线", deviceId);

        AndroidDevice androidDevice = AndroidDeviceHolder.get(deviceId);
        if (androidDevice == null) {
            log.info("[{}]首次上线", deviceId);
            log.info("[{}]检查是否已接入过系统", deviceId);
            Device device = serverApi.getDeviceById(deviceId);
            if (device == null) {
                log.info("[{}]首次接入系统", deviceId);
                androidDevice = initDevice(iDevice);
            } else {
                log.info("[{}]已接入过系统", deviceId);
                androidDevice = new AndroidDevice(device, iDevice);
            }
            AndroidDeviceHolder.add(deviceId, androidDevice);
        }

        Device device = androidDevice.getDevice();

        device.setAgentIp(NetUtil.getLocalHostAddress());
        device.setAgentPort(Integer.parseInt(App.getProperty("server.port")));
        device.setStatus(Device.IDLE_STATUS);
        device.setLastOnlineTime(new Date());

        serverApi.saveDevice(device);
        log.info("[{}]deviceConnected处理完成", deviceId);
    }

    /**
     * Android设备断开电脑，调用的方法
     *
     * @param iDevice
     */
    public void androidDeviceDisconnected(IDevice iDevice) {
        String deviceId = iDevice.getSerialNumber();
        log.info("[{}]断开连接", deviceId);

        AndroidDevice androidDevice = AndroidDeviceHolder.get(deviceId);
        if (androidDevice == null) {
            return;
        }

        //todo 重构这里
        //手机断开 回收minicap/minitouch/adbkit等占用的资源，如关闭输入输出流，端口释放等
        StfResourceReleaser stfResourceReleaser = new StfResourceReleaser(deviceId);
        stfResourceReleaser.release();

        Device device = androidDevice.getDevice();
        device.setStatus(Device.OFFLINE_STATUS);
        device.setLastOfflineTime(new Date());

        serverApi.saveDevice(device);
        log.info("[{}]deviceDisconnected处理完成", deviceId);
    }

    /**
     * 首次接入系统，初始化Device
     *
     * @param iDevice
     * @return
     */
    private AndroidDevice initDevice(IDevice iDevice) throws Exception {
        File screenshot = null;
        try {
            Device device = new Device();

            device.setType(Device.ANDROID_TYPE);
            device.setCreateTime(new Date());
            device.setId(iDevice.getSerialNumber());
            device.setCpuInfo(AndroidUtils.getCpuInfo(iDevice));
            device.setMemSize(AndroidUtils.getMemSize(iDevice));
            device.setName(AndroidUtils.getDeviceName(iDevice));
            device.setSystemVersion(AndroidUtils.getAndroidVersion(iDevice));

            String[] resolution = AndroidUtils.getResolution(iDevice).split("x");
            device.setScreenWidth(Integer.parseInt(resolution[0]));
            device.setScreenHeight(Integer.parseInt(resolution[1]));

            //截图并上传到服务器
            screenshot = AndroidUtils.screenshot(iDevice);
            String downloadURL = serverApi.uploadFile(screenshot);
            device.setImgUrl(downloadURL);

            AndroidDevice androidDevice = new AndroidDevice(device, iDevice);

            //安装minicap minitouch uiautomatorServerApk
            installMinicapAndMinitouchAndUiAutomatorServerApk(androidDevice);

            device.setStfStatus(Device.STF_SUCCESS_STATUS);
            device.setMacacaStatus(Device.MACACA_SUCCESS_STATUS);

            return androidDevice;
        } finally {
            //删除首次接入系统的截图
            FileUtils.deleteQuietly(screenshot);
        }
    }

    /**
     * 安装minicap minitouch uiautomatorServerApk
     *
     * @param androidDevice
     */
    private void installMinicapAndMinitouchAndUiAutomatorServerApk(AndroidDevice androidDevice) throws Exception {
        String deviceId = androidDevice.getDevice().getId();
        IDevice iDevice = androidDevice.getIDevice();

        log.info("[{}]开始安装minicap", deviceId);
        MinicapInstaller minicapInstaller = new MinicapInstaller(iDevice);
        minicapInstaller.install();
        log.info("[{}]安装minicap成功", deviceId);

        log.info("[{}]开始安装minitouch", deviceId);
        MinitouchInstaller minitouchInstaller = new MinitouchInstaller(iDevice);
        minitouchInstaller.install();
        log.info("[{}]安装minitouch成功", deviceId);

        log.info("[{}]开始安装UiautomatorServerApk", deviceId);
        Uiautomator2ServerApkInstaller uiautomator2ServerApkInstaller = new Uiautomator2ServerApkInstaller(iDevice);
        uiautomator2ServerApkInstaller.install();
        log.info("[{}]安装UiautomatorServerApk成功", deviceId);
    }
}
