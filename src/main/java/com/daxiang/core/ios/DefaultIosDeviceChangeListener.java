package com.daxiang.core.ios;

import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceChangeHandler;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.appium.AppiumServer;
import com.daxiang.model.Device;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Dimension;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
public class DefaultIosDeviceChangeListener extends MobileDeviceChangeHandler implements IosDeviceChangeListener {

    @Override
    public void onDeviceConnected(String deviceId) {
        new Thread(() -> iosDeviceConnected(deviceId)).start();
    }

    @Override
    public void onDeviceDisconnected(String deviceId) {
        new Thread(() -> iosDeviceDisconnected(deviceId)).start();
    }

    private void iosDeviceConnected(String deviceId) {
        log.info("[ios][{}]已连接", deviceId);

        MobileDevice mobileDevice = MobileDeviceHolder.get(deviceId);
        if (mobileDevice == null) {
            log.info("[ios][{}]首次在agent上线", deviceId);

            log.info("[ios][{}]启动appium server...", deviceId);
            AppiumServer appiumServer = new AppiumServer();
            appiumServer.start();
            log.info("[ios][{}]启动appium server完成，url: {}", deviceId, appiumServer.getUrl());

            log.info("[ios][{}]检查是否已接入过master", deviceId);
            Device device = getDeviceById(deviceId);
            if (device == null) {
                log.info("[ios][{}]首次接入master，开始初始化设备", deviceId);
                try {
                    mobileDevice = initIosDevice(deviceId, appiumServer);
                    log.info("[ios][{}]初始化设备完成", deviceId);
                } catch (Exception e) {
                    throw new RuntimeException("初始化设备" + deviceId + "出错", e);
                }
            } else {
                log.info("[ios][{}]已接入过master", deviceId);
                mobileDevice = new IosDevice(device, appiumServer);
            }

            MobileDeviceHolder.add(deviceId, mobileDevice);
        } else {
            log.info("[ios][{}]非首次在agent上线", deviceId);
        }

        mobileOnline(mobileDevice);
        log.info("[ios][{}]iosDeviceConnected处理完成", deviceId);
    }

    private void iosDeviceDisconnected(String deviceId) {
        log.info("[ios][{}]断开连接", deviceId);
        mobileDisconnected(deviceId);
        log.info("[ios][{}]iosDeviceDisconnected处理完成", deviceId);
    }

    private MobileDevice initIosDevice(String deviceId, AppiumServer appiumServer) throws Exception {
        Device device = new Device();

        device.setPlatform(MobileDevice.IOS);
        device.setCreateTime(new Date());
        device.setId(deviceId);
        device.setSystemVersion(IosUtil.getSystemVersion(deviceId));
        device.setName(IosUtil.getDeviceName(deviceId));

        String msg = "请根据productType：" + IosUtil.getProductType(deviceId) + "查出相应的信息，补充到device表";
        device.setCpuInfo(msg);
        device.setMemSize(msg);

        IosDevice iosDevice = new IosDevice(device, appiumServer);

        // 截图并上传到服务器
        String imgDownloadUrl = iosDevice.screenshotAndUploadToMaster();
        device.setImgUrl(imgDownloadUrl);

        log.info("[ios][{}]开始初始化appium", device.getId());
        AppiumDriver appiumDriver = iosDevice.newAppiumDriver();
        appiumDriver.quit();
        log.info("[ios][{}]初始化appium完成", device.getId());

        Dimension size = appiumDriver.manage().window().getSize();
        device.setScreenWidth(size.getWidth());
        device.setScreenHeight(size.getHeight());

        return iosDevice;
    }
}