package com.daxiang.core.mobile.ios;

import com.daxiang.model.Mobile;
import com.daxiang.server.ServerClient;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.mobile.appium.AppiumServer;
import com.daxiang.model.UploadFile;
import com.daxiang.service.MobileService;
import com.daxiang.websocket.WebSocketSessionPool;
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
    public void onDeviceConnected(String mobileId, boolean isRealDevice) {
        new Thread(() -> iosDeviceConnected(mobileId, isRealDevice)).start();
    }

    @Override
    public void onDeviceDisconnected(String mobileId, boolean isRealDevice) {
        new Thread(() -> iosDeviceDisconnected(mobileId, isRealDevice)).start();
    }

    private void iosDeviceConnected(String mobileId, boolean isRealDevice) {
        log.info("[ios][{}]已连接, 是否真机: {}", mobileId, isRealDevice);

        MobileDevice mobileDevice = MobileDeviceHolder.get(mobileId);
        if (mobileDevice == null) {
            log.info("[ios][{}]首次在agent上线", mobileId);

            log.info("[ios][{}]启动appium server...", mobileId);
            AppiumServer appiumServer = new AppiumServer();
            appiumServer.start();
            log.info("[ios][{}]启动appium server完成，url: {}", mobileId, appiumServer.getUrl());

            log.info("[ios][{}]检查是否已接入过server", mobileId);
            Mobile mobile = serverClient.getMobileById(mobileId);
            if (mobile == null) {
                log.info("[ios][{}]首次接入server，开始初始化设备", mobileId);
                try {
                    mobileDevice = initIosDevice(mobileId, isRealDevice, appiumServer);
                    log.info("[ios][{}]初始化设备完成", mobileId);
                } catch (Exception e) {
                    appiumServer.stop();
                    throw new RuntimeException("初始化设备" + mobileId + "出错", e);
                }
            } else {
                log.info("[ios][{}]已接入过server", mobileId);
                mobileDevice = new IosDevice(mobile, appiumServer);
            }

            MobileDeviceHolder.add(mobileId, mobileDevice);
        } else {
            log.info("[ios][{}]非首次在agent上线", mobileId);
        }

        mobileService.saveOnlineDeviceToServer(mobileDevice);
        log.info("[ios][{}]iosDeviceConnected处理完成", mobileId);
    }

    private void iosDeviceDisconnected(String mobileId, boolean isRealDevice) {
        log.info("[ios][{}]断开连接, 是否真机: {}", mobileId, isRealDevice);
        MobileDevice mobileDevice = MobileDeviceHolder.get(mobileId);
        if (mobileDevice == null) {
            return;
        }

        mobileService.saveOfflineDeviceToServer(mobileDevice);

        // 有人正在使用，则断开连接
        Session openedSession = WebSocketSessionPool.getOpenedSession(mobileId);
        if (openedSession != null) {
            try {
                log.info("[ios][{}]sessionId: {}正在使用，关闭连接", mobileId, openedSession.getId());
                openedSession.close();
            } catch (IOException e) {
                log.error("close opened session err", e);
            }
        }

        log.info("[ios][{}]iosDeviceDisconnected处理完成", mobileId);
    }

    private MobileDevice initIosDevice(String mobileId, boolean isRealDevice, AppiumServer appiumServer) throws Exception {
        Mobile mobile = new Mobile();

        mobile.setPlatform(MobileDevice.IOS);
        mobile.setCreateTime(new Date());
        mobile.setId(mobileId);
        mobile.setName(IosUtil.getDeviceName(mobileId, isRealDevice));

        if (isRealDevice) {
            mobile.setSystemVersion(IosUtil.getRealDeviceSystemVersion(mobileId));
        }

        IosDevice iosDevice = new IosDevice(mobile, appiumServer);

        log.info("[ios][{}]开始初始化appium", mobileId);
        AppiumDriver appiumDriver = iosDevice.freshAppiumDriver(null);
        log.info("[ios][{}]初始化appium完成", mobileId);

        if (!isRealDevice) {
            try {
                String sdkVersion = (String) appiumDriver.getSessionDetail("sdkVersion");
                mobile.setSystemVersion(sdkVersion);
            } catch (Exception e) {
                log.warn("[ios][{}]获取sdkVersion失败", mobileId, e);
            }
        }

        // 有时window获取的宽高可能为0
        while (true) {
            Dimension window = appiumDriver.manage().window().getSize();
            int width = window.getWidth();
            int height = window.getHeight();

            if (width > 0 && height > 0) {
                mobile.setScreenWidth(width);
                mobile.setScreenHeight(height);
                break;
            } else {
                log.warn("[ios][{}]未获取到正确的屏幕宽高: {}", mobileId, window);
            }
        }

        // 截图并上传到服务器
        UploadFile uploadFile = iosDevice.screenshotAndUploadToServer();
        mobile.setImgPath(uploadFile.getFilePath());

        appiumDriver.quit();
        return iosDevice;
    }
}
