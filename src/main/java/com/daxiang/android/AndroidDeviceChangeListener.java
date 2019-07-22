package com.daxiang.android;

import com.android.ddmlib.*;
import com.daxiang.android.stf.AdbKit;
import com.daxiang.android.stf.Minicap;
import com.daxiang.android.stf.MinicapInstaller;
import com.daxiang.android.stf.Minitouch;
import com.daxiang.android.stf.MinitouchInstaller;
import com.daxiang.api.MasterApi;
import com.daxiang.appium.AndroidDriverFactory;
import com.daxiang.appium.AppiumServer;
import com.daxiang.model.Device;
import com.daxiang.model.Platform;
import com.daxiang.service.AndroidService;
import io.appium.java_client.android.AndroidDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Date;

/**
 * Created by jiangyitao.
 */
@Component
@Slf4j
public class AndroidDeviceChangeListener implements AndroidDebugBridge.IDeviceChangeListener {

    @Autowired
    private MasterApi masterApi;
    @Autowired
    private AndroidService androidService;
    @Value("${server.address}")
    private String ip;
    @Value("${server.port}")
    private Integer port;

    @Override
    public void deviceConnected(IDevice device) {
        new Thread(() -> androidDeviceConnected(device)).start();
    }

    @Override
    public void deviceDisconnected(IDevice device) {
        new Thread(() -> androidDeviceDisconnected(device)).start();
    }

    @Override
    public void deviceChanged(IDevice device, int changeMask) {
    }

    /**
     * Android设备连接到电脑
     *
     * @param iDevice
     */
    private void androidDeviceConnected(IDevice iDevice) {
        String deviceId = iDevice.getSerialNumber();
        log.info("[{}]已连接", deviceId);

        log.info("[{}]等待手机上线", deviceId);
        AndroidUtil.waitForDeviceOnline(iDevice, 5 * 60);
        log.info("[{}]手机已上线", deviceId);

        AndroidDevice androidDevice = AndroidDeviceHolder.get(deviceId);
        if (androidDevice == null) {
            log.info("[{}]首次在agent上线", deviceId);

            log.info("[{}]启动appium server...", deviceId);
            AppiumServer appiumServer = new AppiumServer();
            appiumServer.start();
            log.info("[{}]启动appium server完成，url: {}", deviceId, appiumServer.getUrl());

            log.info("[{}]检查是否已接入过master", deviceId);
            Device device = masterApi.getDeviceById(deviceId);
            if (device == null) {
                log.info("[{}]首次接入master，开始初始化设备", deviceId);
                try {
                    androidDevice = initDevice(iDevice, appiumServer.getUrl());
                    log.info("[{}]初始化设备完成", deviceId);
                } catch (Exception e) {
                    throw new RuntimeException("初始化设备" + deviceId + "出错", e);
                }
            } else {
                log.info("[{}]已接入过master", deviceId);
                androidDevice = new AndroidDevice(device, iDevice);
            }

            androidDevice.setAppiumServer(appiumServer);
            androidDevice.setMinicap(new Minicap(androidDevice));
            androidDevice.setMinitouch(new Minitouch(androidDevice));
            androidDevice.setAdbKit(new AdbKit(androidDevice));

            AndroidDeviceHolder.add(deviceId, androidDevice);
        } else {
            log.info("[{}]非首次在agent上线", deviceId);
        }

        Device device = androidDevice.getDevice();
        device.setAgentIp(ip);
        device.setAgentPort(port);
        device.setStatus(Device.IDLE_STATUS);
        device.setLastOnlineTime(new Date());

        masterApi.saveDevice(device);
        log.info("[{}]deviceConnected处理完成", deviceId);
    }

    /**
     * Android设备断开电脑
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

        Device device = androidDevice.getDevice();
        device.setStatus(Device.OFFLINE_STATUS);
        device.setLastOfflineTime(new Date());

        masterApi.saveDevice(device);
        log.info("[{}]deviceDisconnected处理完成", deviceId);
    }

    /**
     * 首次接入系统，初始化设备
     */
    private AndroidDevice initDevice(IDevice iDevice, URL url) throws Exception {
        String deviceId = iDevice.getSerialNumber();

        log.info("[{}]开始安装minicap", deviceId);
        MinicapInstaller minicapInstaller = new MinicapInstaller(iDevice);
        minicapInstaller.install();
        log.info("[{}]安装minicap成功", deviceId);

        log.info("[{}]开始安装minitouch", deviceId);
        MinitouchInstaller minitouchInstaller = new MinitouchInstaller(iDevice);
        minitouchInstaller.install();
        log.info("[{}]安装minitouch成功", deviceId);

        Device device = new Device();

        device.setPlatform(Platform.ANDROID);
        device.setCreateTime(new Date());
        device.setId(deviceId);
        try {
            device.setCpuInfo(AndroidUtil.getCpuInfo(iDevice));
        } catch (Exception e) {
            log.error("获取cpu信息失败", e);
            device.setCpuInfo("获取cpu信息失败");
        }
        try {
            device.setMemSize(AndroidUtil.getMemSize(iDevice));
        } catch (Exception e) {
            log.error("获取内存大小失败", e);
            device.setMemSize("获取内存大小失败");
        }
        device.setName(AndroidUtil.getDeviceName(iDevice));
        device.setSystemVersion(AndroidUtil.getAndroidVersion(iDevice));

        String resolution = AndroidUtil.getResolution(iDevice);
        String[] resolutionArray = resolution.split("x");
        device.setScreenWidth(Integer.parseInt(resolutionArray[0]));
        device.setScreenHeight(Integer.parseInt(resolutionArray[1]));

        // 截图并上传到服务器
        String imgDownloadUrl = androidService.screenshotByMinicapAndUploadToMaster(iDevice, resolution);
        device.setImgUrl(imgDownloadUrl);

        AndroidDevice androidDevice = new AndroidDevice(device, iDevice);

        // 安装一个测试apk，用于初始化appium driver
        AndroidUtil.installApk(iDevice, "vendor/apk/ApiDemos-debug.apk");

        log.info("[{}]开始初始化appium", device.getId());
        AndroidDriver androidDriver = AndroidDriverFactory.create(androidDevice, url);
        androidDevice.setAppiumDriver(androidDriver);
        log.info("[{}]初始化appium完成", device.getId());

        return androidDevice;
    }
}
