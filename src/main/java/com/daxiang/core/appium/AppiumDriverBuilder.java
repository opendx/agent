package com.daxiang.core.appium;

import com.daxiang.core.MobileDevice;
import com.daxiang.core.PortProvider;
import com.daxiang.core.android.AndroidDevice;
import com.daxiang.core.ios.IosUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.*;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;

/**
 * Created by jiangyitao.
 */
public class AppiumDriverBuilder {

    private static final String APP_PACKAGE = "io.appium.android.apis";
    private static final String APP_ACTIVITY = "io.appium.android.apis.ApiDemos";
    private static final String BUNDLE_ID = "com.apple.mobilesafari";

    /**
     * 一个session两次命令之间最大允许12小时间隔，超出12小时，session将会被appium server删除
     */
    private static final int NEW_COMMAND_TIMEOUT = 60 * 60 * 12;

    public static AppiumDriver build(MobileDevice mobileDevice, URL url) {
        boolean isAndroid = false;
        if (mobileDevice instanceof AndroidDevice) {
            isAndroid = true;
        }

        // http://appium.io/docs/en/writing-running-appium/caps/
        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, NEW_COMMAND_TIMEOUT);
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, mobileDevice.getDevice().getName());
        capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, mobileDevice.getDevice().getSystemVersion());
        capabilities.setCapability(MobileCapabilityType.UDID, mobileDevice.getId());
        capabilities.setCapability(MobileCapabilityType.NO_RESET, true); // http://appium.io/docs/en/writing-running-appium/other/reset-strategies/index.html

        if (isAndroid) {
            capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
            capabilities.setCapability(AndroidMobileCapabilityType.UNICODE_KEYBOARD, true); // 切换到appium输入法
            if (((AndroidDevice) mobileDevice).canUseUiautomator2()) {
                capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2); // UIAutomation2 is only supported since Android 5.0 (Lollipop)
            } else {
                capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UIAutomator1");
            }
            capabilities.setCapability(AndroidMobileCapabilityType.SYSTEM_PORT, PortProvider.getUiautomator2ServerAvailablePort());
            // todo 如果手机的chrome版本与appium默认的chromedriver不匹配，导致切换webview出错，可以指定chromeDriver位置
            // capabilities.setCapability(AndroidMobileCapabilityType.CHROMEDRIVER_EXECUTABLE, "");
            capabilities.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, APP_PACKAGE);
            capabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, APP_ACTIVITY);
            capabilities.setCapability(AndroidMobileCapabilityType.NO_SIGN, true);
            capabilities.setCapability(AndroidMobileCapabilityType.AUTO_GRANT_PERMISSIONS, true);
            capabilities.setCapability("autoLaunch", false);
            capabilities.setCapability("skipLogcatCapture", true);
            return new AndroidDriver(url, capabilities);
        } else {
            capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.IOS);
            capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.IOS_XCUI_TEST);
            capabilities.setCapability(IOSMobileCapabilityType.WDA_LOCAL_PORT, PortProvider.getWdaLocalAvailablePort());
            capabilities.setCapability("mjpegServerPort", PortProvider.getWdaMjpegServerAvailablePort());
            capabilities.setCapability(IOSMobileCapabilityType.BUNDLE_ID, BUNDLE_ID);
            capabilities.setCapability("waitForQuiescence", false);
            capabilities.setCapability("skipLogCapture", true);
            IOSDriver iosDriver = new IOSDriver(url, capabilities);
            IosUtil.pressHome(iosDriver);
            return iosDriver;
        }
    }
}