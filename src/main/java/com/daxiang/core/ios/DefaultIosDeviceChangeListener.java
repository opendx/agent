package com.daxiang.core.ios;

import com.daxiang.server.ServerClient;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.appium.AppiumServer;
import com.daxiang.model.Device;
import com.daxiang.model.UploadFile;
import com.daxiang.service.MobileService;
import com.daxiang.websocket.MobileDeviceWebSocketSessionPool;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Dimension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Date;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
public class DefaultIosDeviceChangeListener implements IosDeviceChangeListener {

    @Autowired
    private ServerClient serverClient;
    @Autowired
    private MobileService mobileService;

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

            log.info("[ios][{}]检查是否已接入过server", deviceId);
            Device device = serverClient.getDeviceById(deviceId);
            if (device == null) {
                log.info("[ios][{}]首次接入server，开始初始化设备", deviceId);
                try {
                    mobileDevice = initIosDevice(deviceId, appiumServer);
                    log.info("[ios][{}]初始化设备完成", deviceId);
                } catch (Exception e) {
                    appiumServer.stop();
                    throw new RuntimeException("初始化设备" + deviceId + "出错", e);
                }
            } else {
                log.info("[ios][{}]已接入过server", deviceId);
                mobileDevice = new IosDevice(device, appiumServer);
            }

            MobileDeviceHolder.add(deviceId, mobileDevice);
        } else {
            log.info("[ios][{}]非首次在agent上线", deviceId);
        }

        mobileService.saveOnlineDeviceToServer(mobileDevice);
        log.info("[ios][{}]iosDeviceConnected处理完成", deviceId);
    }

    private void iosDeviceDisconnected(String deviceId) {
        log.info("[ios][{}]断开连接", deviceId);
        MobileDevice mobileDevice = MobileDeviceHolder.get(deviceId);
        if (mobileDevice == null) {
            return;
        }

        mobileService.saveOfflineDeviceToServer(mobileDevice);

        // 有人正在使用，则断开连接
        Session openedSession = MobileDeviceWebSocketSessionPool.getOpenedSession(deviceId);
        if (openedSession != null) {
            try {
                log.info("[ios][{}]sessionId: {}正在使用，关闭连接", deviceId, openedSession.getId());
                openedSession.close();
            } catch (IOException e) {
                log.error("close opened session err", e);
            }
        }

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

        log.info("[ios][{}]开始初始化appium", deviceId);
        AppiumDriver appiumDriver = iosDevice.freshAppiumDriver(null);
        log.info("[ios][{}]初始化appium完成", deviceId);

        // 有时window获取的宽高可能为0
        while (true) {
            Dimension window = appiumDriver.manage().window().getSize();
            int width = window.getWidth();
            int height = window.getHeight();

            if (width > 0 && height > 0) {
                device.setScreenWidth(width);
                device.setScreenHeight(height);
                break;
            } else {
                log.warn("[ios][{}]未获取到正确的屏幕宽高: {}", deviceId, window);
            }
        }

        // 截图并上传到服务器
        UploadFile uploadFile = iosDevice.screenshotAndUploadToServer();
        device.setImgPath(uploadFile.getFilePath());

        appiumDriver.quit();
        return iosDevice;
    }
}
