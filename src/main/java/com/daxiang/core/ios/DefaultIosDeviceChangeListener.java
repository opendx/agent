package com.daxiang.core.ios;

import com.daxiang.api.MasterApi;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.appium.AppiumDriverFactory;
import com.daxiang.core.appium.AppiumServer;
import com.daxiang.model.Device;
import com.daxiang.service.IosService;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Date;


/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
public class DefaultIosDeviceChangeListener implements IosDeviceChangeListener {

    @Autowired
    private MasterApi masterApi;
    @Autowired
    private IosService iosService;
    @Value("${server.address}")
    private String ip;
    @Value("${server.port}")
    private Integer port;

    @Override
    public void onDeviceConnected(String deviceId) {
        new Thread(() -> iosDeviceConnected(deviceId)).start();
    }

    @Override
    public void onDeviceDisConnected(String deviceId) {
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
            Device device = masterApi.getDeviceById(deviceId);
            if (device == null) {
                log.info("[ios][{}]首次接入master，开始初始化设备", deviceId);
                try {
                    mobileDevice = initDevice(deviceId, appiumServer.getUrl());
                    log.info("[ios][{}]初始化设备完成", deviceId);
                } catch (Exception e) {
                    throw new RuntimeException("初始化设备" + deviceId + "出错", e);
                }
            } else {
                log.info("[ios][{}]已接入过master", deviceId);
                mobileDevice = new IosDevice(device);
            }

            mobileDevice.setAppiumServer(appiumServer);
            MobileDeviceHolder.add(deviceId, mobileDevice);
        } else {
            log.info("[ios][{}]非首次在agent上线", deviceId);
        }

        Device device = mobileDevice.getDevice();
        device.setAgentIp(ip);
        device.setAgentPort(port);
        device.setStatus(Device.IDLE_STATUS);
        device.setLastOnlineTime(new Date());

        masterApi.saveDevice(device);
        log.info("[ios][{}]deviceConnected处理完成", deviceId);
    }

    private void iosDeviceDisconnected(String deviceId) {
        log.info("[ios][{}]断开连接", deviceId);

        MobileDevice mobileDevice = MobileDeviceHolder.get(deviceId);
        if (mobileDevice == null) {
            return;
        }

        Device device = mobileDevice.getDevice();
        device.setStatus(Device.OFFLINE_STATUS);
        device.setLastOfflineTime(new Date());

        masterApi.saveDevice(device);
        log.info("[ios][{}]deviceDisconnected处理完成", deviceId);
    }

    public MobileDevice initDevice(String deviceId, URL url) throws Exception {
        Device device = new Device();

        device.setPlatform(MobileDevice.IOS);
        device.setCreateTime(new Date());
        device.setId(deviceId);
        device.setSystemVersion(IosUtil.getSystemVersion(deviceId));

        String productType = IosUtil.getProductType(deviceId);
        // todo master findbyProductType
//        device.setCpuInfo();
//        device.setMemSize();
//        device.setName();
//        device.setScreenWidth();
//        device.setScreenHeight();

        // 截图并上传到服务器
        String imgDownloadUrl = iosService.screenshotAndUploadToMaster(deviceId);
        device.setImgUrl(imgDownloadUrl);

        IosDevice iosDevice = new IosDevice(device);

        log.info("[ios][{}]开始初始化appium", device.getId());
        AppiumDriver appiumDriver = AppiumDriverFactory.create(iosDevice, url);
        iosDevice.setAppiumDriver(appiumDriver);
        log.info("[ios][{}]初始化appium完成", device.getId());

        return iosDevice;
    }
}