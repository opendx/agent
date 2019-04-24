package com.fgnb.android.uiautomator;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class Uiautomator2ServerApkInstaller {

    private static final String APP_DEBUG_APK_PATH = "vendor/macaca/app-debug.apk";
    private static final String APP_DEBUG_ANDROID_TEST_APK_PATH = "vendor/macaca/app-debug-androidTest.apk";

    private IDevice iDevice;

    public Uiautomator2ServerApkInstaller(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    /**
     * 安装uiautomator2server apk
     */
    public void install() throws InstallException {
        String deviceId = iDevice.getSerialNumber();

        log.info("[{}][uiautomator2server]开始安装{}", deviceId, APP_DEBUG_APK_PATH);
        iDevice.installPackage(APP_DEBUG_APK_PATH, true, "-t");
        log.info("[{}][uiautomator2server]安装{}完成", deviceId, APP_DEBUG_APK_PATH);

        log.info("[{}][uiautomator2server]开始安装{}", deviceId, APP_DEBUG_ANDROID_TEST_APK_PATH);
        iDevice.installPackage(APP_DEBUG_ANDROID_TEST_APK_PATH, true, "-t");
        log.info("[{}][uiautomator2server]安装{}完成", deviceId, APP_DEBUG_ANDROID_TEST_APK_PATH);
    }
}
