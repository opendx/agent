package com.daxiang.core.android;

import com.android.ddmlib.*;
import com.daxiang.core.MobileDeviceChangeHandler;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.android.stf.AdbKit;
import com.daxiang.core.android.stf.Minicap;
import com.daxiang.core.android.stf.MinicapInstaller;
import com.daxiang.core.android.stf.Minitouch;
import com.daxiang.core.android.stf.MinitouchInstaller;
import com.daxiang.core.appium.AppiumDriverBuilder;
import com.daxiang.core.appium.AppiumServer;
import com.daxiang.model.Device;
import com.daxiang.service.AndroidService;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Date;

/**
 * Created by jiangyitao.
 */
@Component
@Slf4j
public class AndroidDeviceChangeListener extends MobileDeviceChangeHandler implements AndroidDebugBridge.IDeviceChangeListener {

    @Autowired
    private AndroidService androidService;

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
        log.info("[android][{}]已连接", deviceId);

        log.info("[android][{}]等待手机上线", deviceId);
        AndroidUtil.waitForDeviceOnline(iDevice, 5 * 60);
        log.info("[android][{}]手机已上线", deviceId);

        MobileDevice mobileDevice = MobileDeviceHolder.get(deviceId);
        if (mobileDevice == null) {
            log.info("[android][{}]首次在agent上线", deviceId);

            log.info("[android][{}]启动appium server...", deviceId);
            AppiumServer appiumServer = new AppiumServer();
            appiumServer.start();
            log.info("[android][{}]启动appium server完成，url: {}", deviceId, appiumServer.getUrl());

            log.info("[android][{}]检查是否已接入过master", deviceId);
            Device device = getDeviceById(deviceId);
            if (device == null) {
                log.info("[android][{}]首次接入master，开始初始化设备", deviceId);
                try {
                    mobileDevice = initAndroidDevice(iDevice, appiumServer.getUrl());
                    log.info("[android][{}]初始化设备完成", deviceId);
                } catch (Exception e) {
                    throw new RuntimeException("初始化设备" + deviceId + "出错", e);
                }
            } else {
                log.info("[android][{}]已接入过master", deviceId);
                mobileDevice = new AndroidDevice(device, iDevice);
            }

            AndroidDevice androidDevice = (AndroidDevice) mobileDevice;
            androidDevice.setMinicap(new Minicap(androidDevice));
            androidDevice.setMinitouch(new Minitouch(androidDevice));
            androidDevice.setAdbKit(new AdbKit(androidDevice));

            mobileDevice.setAppiumServer(appiumServer);
            MobileDeviceHolder.add(deviceId, mobileDevice);
        } else {
            log.info("[android][{}]非首次在agent上线", deviceId);
        }

        mobileOnline(mobileDevice);
        log.info("[android][{}]deviceConnected处理完成", deviceId);
    }

    /**
     * Android设备断开电脑
     *
     * @param iDevice
     */
    public void androidDeviceDisconnected(IDevice iDevice) {
        String deviceId = iDevice.getSerialNumber();
        log.info("[android][{}]断开连接", deviceId);
        mobileDisconnected(deviceId);
        log.info("[android][{}]deviceDisconnected处理完成", deviceId);
    }

    /**
     * 首次接入系统，初始化Android设备
     */
    private MobileDevice initAndroidDevice(IDevice iDevice, URL url) throws Exception {
        String deviceId = iDevice.getSerialNumber();

        log.info("[android][{}]开始安装minicap", deviceId);
        MinicapInstaller minicapInstaller = new MinicapInstaller(iDevice);
        minicapInstaller.install();
        log.info("[android][{}]安装minicap成功", deviceId);

        log.info("[android][{}]开始安装minitouch", deviceId);
        MinitouchInstaller minitouchInstaller = new MinitouchInstaller(iDevice);
        minitouchInstaller.install();
        log.info("[android][{}]安装minitouch成功", deviceId);

        Device device = new Device();

        device.setPlatform(MobileDevice.ANDROID);
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

        log.info("[android][{}]开始初始化appium", device.getId());
        AppiumDriver appiumDriver = AppiumDriverBuilder.build(androidDevice, url);
        androidDevice.setAppiumDriver(appiumDriver);
        log.info("[android][{}]初始化appium完成", device.getId());

        return androidDevice;
    }
}