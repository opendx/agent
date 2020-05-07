package com.daxiang.core.mobile.android;

import com.alibaba.fastjson.JSONObject;
import com.android.ddmlib.*;
import com.daxiang.model.Mobile;
import com.daxiang.server.ServerClient;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.mobile.android.scrcpy.Scrcpy;
import com.daxiang.core.mobile.android.stf.AdbKit;
import com.daxiang.core.mobile.android.stf.Minicap;
import com.daxiang.core.mobile.android.stf.MinicapInstaller;
import com.daxiang.core.mobile.android.stf.Minitouch;
import com.daxiang.core.mobile.android.stf.MinitouchInstaller;
import com.daxiang.core.mobile.appium.AppiumServer;
import com.daxiang.model.UploadFile;
import com.daxiang.service.MobileService;
import com.daxiang.websocket.WebSocketSessionPool;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by jiangyitao.
 */
@Component
@Slf4j
public class AndroidDeviceChangeListener implements AndroidDebugBridge.IDeviceChangeListener {

    // http://appium.github.io/appium/assets/ApiDemos-debug.apk
    private static final String APIDEMOS_APK = "vendor/apk/ApiDemos-debug.apk";

    @Autowired
    private ServerClient serverClient;
    @Autowired
    private MobileService mobileService;

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
        String mobileId = iDevice.getSerialNumber();
        log.info("[android][{}]已连接", mobileId);

        log.info("[android][{}]等待上线", mobileId);
        AndroidUtil.waitForDeviceOnline(iDevice, 5 * 60);
        log.info("[android][{}]已上线", mobileId);

        MobileDevice mobileDevice = MobileDeviceHolder.get(mobileId);
        if (mobileDevice == null) {
            log.info("[android][{}]首次在agent上线", mobileId);

            log.info("[android][{}]启动appium server...", mobileId);
            AppiumServer appiumServer = new AppiumServer();
            appiumServer.start();
            log.info("[android][{}]启动appium server完成，url: {}", mobileId, appiumServer.getUrl());

            log.info("[android][{}]检查是否已接入过server", mobileId);
            Mobile mobile = serverClient.getMobileById(mobileId);
            if (mobile == null) {
                log.info("[android][{}]首次接入server，开始初始化设备", mobileId);
                try {
                    mobileDevice = initAndroidDevice(iDevice, appiumServer);
                    log.info("[android][{}]初始化设备完成", mobileId);
                } catch (Exception e) {
                    appiumServer.stop();
                    throw new RuntimeException("初始化设备" + mobileId + "出错", e);
                }
            } else {
                log.info("[android][{}]已接入过server", mobileId);
                mobileDevice = new AndroidDevice(mobile, iDevice, appiumServer);
            }

            AndroidDevice androidDevice = (AndroidDevice) mobileDevice;
            androidDevice.setMinicap(new Minicap(iDevice));
            androidDevice.setMinitouch(new Minitouch(iDevice));
            androidDevice.setAdbKit(new AdbKit(mobileId));
            androidDevice.setScrcpy(new Scrcpy(iDevice));

            MobileDeviceHolder.add(mobileId, mobileDevice);
        } else {
            ((AndroidDevice) mobileDevice).setIDevice(iDevice);
            log.info("[android][{}]非首次在agent上线", mobileId);
        }

        mobileService.saveOnlineDeviceToServer(mobileDevice);
        log.info("[android][{}]androidDeviceConnected处理完成", mobileId);
    }

    /**
     * Android设备断开电脑
     *
     * @param iDevice
     */
    public void androidDeviceDisconnected(IDevice iDevice) {
        String mobileId = iDevice.getSerialNumber();
        log.info("[android][{}]断开连接", mobileId);
        MobileDevice mobileDevice = MobileDeviceHolder.get(mobileId);
        if (mobileDevice == null) {
            return;
        }

        mobileService.saveOfflineDeviceToServer(mobileDevice);

        // 有人正在使用，则断开连接
        Session openedSession = WebSocketSessionPool.getOpenedSession(mobileId);
        if (openedSession != null) {
            try {
                log.info("[android][{}]sessionId: {}正在使用，关闭连接", mobileId, openedSession.getId());
                openedSession.close();
            } catch (IOException e) {
                log.error("close opened session err", e);
            }
        }

        log.info("[android][{}]androidDeviceDisconnected处理完成", mobileId);
    }

    /**
     * 首次接入系统，初始化Android设备
     */
    private MobileDevice initAndroidDevice(IDevice iDevice, AppiumServer appiumServer) throws Exception {
        String mobileId = iDevice.getSerialNumber();

        Mobile mobile = new Mobile();

        mobile.setPlatform(MobileDevice.ANDROID);
        mobile.setCreateTime(new Date());
        mobile.setId(mobileId);
        mobile.setSystemVersion(AndroidUtil.getAndroidVersion(AndroidUtil.getSdkVersion(iDevice)));
        mobile.setName(AndroidUtil.getDeviceName(iDevice));
        mobile.setCpuInfo(AndroidUtil.getCpuInfo(iDevice));
        mobile.setMemSize(AndroidUtil.getMemSize(iDevice));

        String resolution = AndroidUtil.getResolution(iDevice); // 720x1280
        String[] res = resolution.split("x");
        mobile.setScreenWidth(Integer.parseInt(res[0]));
        mobile.setScreenHeight(Integer.parseInt(res[1]));

        AndroidDevice androidDevice = new AndroidDevice(mobile, iDevice, appiumServer);

        // 小于android5.0使用stf远程真机方案，否则使用scrcpy方案
        // 小于android5.0初始化driver需要指定app
        if (!androidDevice.greaterOrEqualsToAndroid5()) {
            log.info("[android][{}]开始安装minicap", mobileId);
            MinicapInstaller minicapInstaller = new MinicapInstaller(iDevice);
            minicapInstaller.install();
            log.info("[android][{}]安装minicap成功", mobileId);

            log.info("[android][{}]开始安装minitouch", mobileId);
            MinitouchInstaller minitouchInstaller = new MinitouchInstaller(iDevice);
            minitouchInstaller.install();
            log.info("[android][{}]安装minitouch成功", mobileId);

            // 安装一个测试apk，用于初始化appium driver
            log.info("[android][{}]开始安装{}", mobileId, APIDEMOS_APK);
            androidDevice.installApp(new File(APIDEMOS_APK));
            log.info("[android][{}]安装{}完成", mobileId, APIDEMOS_APK);
        }

        JSONObject caps = new JSONObject();
        caps.put("skipServerInstallation", false);
        caps.put("skipDeviceInitialization", false);
        caps.put("skipUnlock", false);
        caps.put("skipLogcatCapture", false);

        log.info("[android][{}]开始初始化appium", mobileId);
        AppiumDriver appiumDriver = androidDevice.freshAppiumDriver(caps);
        log.info("[android][{}]初始化appium完成", mobileId);

        // 截图并上传到服务器
        UploadFile uploadFile = androidDevice.screenshotAndUploadToServer();
        mobile.setImgPath(uploadFile.getFilePath());

        appiumDriver.quit();

        return androidDevice;
    }
}
