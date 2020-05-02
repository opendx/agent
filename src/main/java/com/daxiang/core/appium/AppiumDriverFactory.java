package com.daxiang.core.appium;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.App;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.PortProvider;
import com.daxiang.core.android.AndroidDevice;
import com.daxiang.core.ios.IosDevice;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.*;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Created by jiangyitao.
 * http://appium.io/docs/en/writing-running-appium/caps/
 */
@Slf4j
public class AppiumDriverFactory {

    /**
     * 调试action需要，两次命令之间最大允许12小时间隔
     */
    private static final int NEW_COMMAND_TIMEOUT = 60 * 60 * 12;

    public static AppiumDriver create(MobileDevice mobileDevice, JSONObject capabilities) {
        if (mobileDevice instanceof AndroidDevice) {
            return createAndroidDriver((AndroidDevice) mobileDevice, capabilities);
        } else if (mobileDevice instanceof IosDevice) {
            return createIosDriver((IosDevice) mobileDevice, capabilities);
        }

        throw new IllegalArgumentException("不支持的设备: " + mobileDevice.getId());
    }

    private static AppiumDriver createAndroidDriver(AndroidDevice androidDevice, JSONObject capabilities) {
        JSONObject caps = new JSONObject();
        caps.put(MobileCapabilityType.NO_RESET, true);
        caps.put(AndroidMobileCapabilityType.UNICODE_KEYBOARD, true);
        caps.put("showChromedriverLog", true);
        caps.put("recreateChromeDriverSessions", true);
        caps.put("extractChromeAndroidPackageFromContextName", true);

        // 加速初始化速度
        caps.put("skipServerInstallation", true);
        caps.put("skipDeviceInitialization", true);
        caps.put("skipUnlock", true);
        caps.put("skipLogcatCapture", true);

        if (!androidDevice.greaterOrEqualsToAndroid5()) { // 小于安卓5，必须指定app，否则会创建driver失败
            caps.put("appPackage", "io.appium.android.apis");
            caps.put("appActivity", "io.appium.android.apis.ApiDemos");
            caps.put("autoLaunch", false);
        }

        // **** 以上caps可被传入的capabilities覆盖 ****

        if (!CollectionUtils.isEmpty(capabilities)) {
            caps.putAll(capabilities);
        }

        // **** 以下caps具有更高优先级，将覆盖传入的capabilities ****

        if (androidDevice.greaterOrEqualsToAndroid5()) {
            caps.put(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2); // UIAutomation2 is only supported since Android 5.0 (Lollipop)
        } else {
            caps.put(MobileCapabilityType.AUTOMATION_NAME, "UIAutomator1");
        }
        caps.put(AndroidMobileCapabilityType.SYSTEM_PORT, PortProvider.getUiautomator2ServerAvailablePort());

        caps.put("chromedriverPort", PortProvider.getAndroidChromeDriverAvailablePort());
        Optional<String> chromedriverFilePath = androidDevice.getChromedriverFilePath();
        if (chromedriverFilePath.isPresent()) {
            caps.put(AndroidMobileCapabilityType.CHROMEDRIVER_EXECUTABLE, chromedriverFilePath.get());
        }

        caps.put(MobileCapabilityType.UDID, androidDevice.getId());
        caps.put(MobileCapabilityType.DEVICE_NAME, androidDevice.getDevice().getName());
        caps.put(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
        caps.put(MobileCapabilityType.PLATFORM_VERSION, androidDevice.getDevice().getSystemVersion());
        caps.put(MobileCapabilityType.NEW_COMMAND_TIMEOUT, NEW_COMMAND_TIMEOUT);

        return new AndroidDriver(androidDevice.getAppiumServer().getUrl(), new DesiredCapabilities(caps));
    }

    private static AppiumDriver createIosDriver(IosDevice iosDevice, JSONObject capabilities) {
        JSONObject caps = new JSONObject();
        caps.put(MobileCapabilityType.NO_RESET, true);
        caps.put("waitForQuiescence", false);
        caps.put("skipLogCapture", true);
        // Get JSON source from WDA and parse into XML on Appium server. This can be much faster, especially on large devices.
        caps.put("useJSONSource", true);
        caps.put(IOSMobileCapabilityType.WDA_STARTUP_RETRIES, 0);
        caps.put("mjpegServerFramerate", Integer.parseInt(App.getProperty("mjpegServerFramerate")));

        // https://github.com/appium/appium-xcuitest-driver/blob/master/docs/real-device-config.md
        String xcodeOrgId = App.getProperty("xcodeOrgId");
        if (!StringUtils.isEmpty(xcodeOrgId)) {
            caps.put("xcodeOrgId", xcodeOrgId);
        }
        String xcodeSigningId = App.getProperty("xcodeSigningId");
        if (!StringUtils.isEmpty(xcodeSigningId)) {
            caps.put("xcodeSigningId", xcodeSigningId);
        }
        String updatedWDABundleId = App.getProperty("updatedWDABundleId");
        if (!StringUtils.isEmpty(updatedWDABundleId)) {
            caps.put("updatedWDABundleId", updatedWDABundleId);
        }

        // **** 以上caps可被传入的capabilities覆盖 ****

        if (CollectionUtils.isEmpty(capabilities)) {
            caps.putAll(capabilities);
        }

        // **** 以下caps具有更高优先级，将覆盖传入的capabilities ****

        caps.put(MobileCapabilityType.AUTOMATION_NAME, AutomationName.IOS_XCUI_TEST);
        caps.put(IOSMobileCapabilityType.WDA_LOCAL_PORT, PortProvider.getWdaLocalAvailablePort());

        caps.put("mjpegServerPort", PortProvider.getWdaMjpegServerAvailablePort());
        caps.put("webkitDebugProxyPort", PortProvider.getWebkitDebugProxyAvalilablePort());

        caps.put(MobileCapabilityType.DEVICE_NAME, iosDevice.getDevice().getName());
        caps.put(MobileCapabilityType.UDID, iosDevice.getId());
        caps.put(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.IOS);
        caps.put(MobileCapabilityType.PLATFORM_VERSION, iosDevice.getDevice().getSystemVersion());
        caps.put(MobileCapabilityType.NEW_COMMAND_TIMEOUT, NEW_COMMAND_TIMEOUT);

        return new IOSDriver(iosDevice.getAppiumServer().getUrl(), new DesiredCapabilities(caps));
    }
}
