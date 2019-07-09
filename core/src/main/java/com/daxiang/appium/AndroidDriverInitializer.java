package com.daxiang.appium;

import com.daxiang.android.AndroidDevice;
import com.daxiang.android.PortProvider;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.MobilePlatform;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by jiangyitao.
 */
public class AndroidDriverInitializer implements AppiumDriverInitializer {

    private static final String APP_PACKAGE = "io.appium.settings";
    private static final String APP_ACTIVITY = ".Settings";

    private AndroidDevice androidDevice;

    public AndroidDriverInitializer(AndroidDevice androidDevice) {
        this.androidDevice = androidDevice;
    }

    @Override
    public AppiumDriver init() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 60 * 60); // 60分钟
        capabilities.setCapability(AndroidMobileCapabilityType.UNICODE_KEYBOARD, true); // 切换到appium输入法
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, androidDevice.getDevice().getName());
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
        capabilities.setCapability(MobileCapabilityType.UDID, androidDevice.getId());
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2);
        capabilities.setCapability(AndroidMobileCapabilityType.SYSTEM_PORT, PortProvider.getUiautomator2ServerPort());

        // todo 如果手机的chrome版本与appium默认的chromedriver不匹配，导致切换webview出错，可以指定chromeDriver位置
        // capabilities.setCapability(AndroidMobileCapabilityType.CHROMEDRIVER_EXECUTABLE, "");
        capabilities.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, APP_PACKAGE);
        capabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, APP_ACTIVITY);
        capabilities.setCapability(MobileCapabilityType.NO_RESET, true); // true代表不清除手机数据
        return new AndroidDriver(androidDevice.getAppiumServer().getUrl(), capabilities);
    }

    public static void main(String[] args) throws MalformedURLException, InterruptedException {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 60 * 60); // 60分钟
        capabilities.setCapability(AndroidMobileCapabilityType.UNICODE_KEYBOARD, true); // 切换到appium输入法
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "123");
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
        capabilities.setCapability(MobileCapabilityType.UDID, "192.168.67.102:5555");
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2);
        capabilities.setCapability(AndroidMobileCapabilityType.SYSTEM_PORT, PortProvider.getUiautomator2ServerPort());

        // todo 如果手机的chrome版本与appium默认的chromedriver不匹配，导致切换webview出错，可以指定chromeDriver位置
        // capabilities.setCapability(AndroidMobileCapabilityType.CHROMEDRIVER_EXECUTABLE, "");
        capabilities.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, APP_PACKAGE);
        capabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, APP_ACTIVITY);
        capabilities.setCapability("autoLaunch", false);
        capabilities.setCapability(MobileCapabilityType.NO_RESET, true); // true代表不清除手机数据
        AndroidDriver androidDriver =  new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);

        Thread.sleep(100000);
    }
}
