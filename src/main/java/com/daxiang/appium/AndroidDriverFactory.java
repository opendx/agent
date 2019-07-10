package com.daxiang.appium;

import com.daxiang.android.AndroidDevice;
import com.daxiang.android.PortProvider;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.MobilePlatform;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;

/**
 * Created by jiangyitao.
 */
public class AndroidDriverFactory {

    private static final String APP_PACKAGE = "io.appium.settings";
    private static final String APP_ACTIVITY = ".Settings";

    /**
     * 一个session，两次命令之间最大允许12小时间隔，超出12小时，session将会被appium server删除
     */
    private static final int NEW_COMMAND_TIMEOUT = 60 * 60 * 12;

    public static AndroidDriver create(AndroidDevice androidDevice, URL url) {
        // http://appium.io/docs/en/writing-running-appium/caps/
        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, NEW_COMMAND_TIMEOUT);
        capabilities.setCapability(AndroidMobileCapabilityType.UNICODE_KEYBOARD, true); // 切换到appium输入法
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, androidDevice.getDevice().getName());
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
        capabilities.setCapability(MobileCapabilityType.UDID, androidDevice.getId());
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2); // UIAutomation2 is only supported since Android 5.0 (Lollipop)
        capabilities.setCapability(AndroidMobileCapabilityType.SYSTEM_PORT, PortProvider.getUiautomator2ServerPort());
        // todo 如果手机的chrome版本与appium默认的chromedriver不匹配，导致切换webview出错，可以指定chromeDriver位置
        // capabilities.setCapability(AndroidMobileCapabilityType.CHROMEDRIVER_EXECUTABLE, "");
        capabilities.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, APP_PACKAGE);
        capabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, APP_ACTIVITY);
        capabilities.setCapability(MobileCapabilityType.NO_RESET, true); // true代表不清除手机数据
        capabilities.setCapability("skipLogcatCapture", true);
        capabilities.setCapability("autoLaunch", false);

        return new AndroidDriver(url, capabilities);
    }
}
