package com.daxiang.core.appium;

import com.daxiang.App;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.PortProvider;
import com.daxiang.core.android.AndroidDevice;
import io.appium.java_client.remote.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Created by jiangyitao.
 */
public class DesiredCapabilitiesBuilder {

    /**
     * 一个session两次命令之间最大允许12小时间隔，超出12小时，session将会被appium server删除
     */
    private static final int NEW_COMMAND_TIMEOUT = 60 * 60 * 12;

    private DesiredCapabilities capabilities;
    private MobileDevice mobileDevice;

    public DesiredCapabilitiesBuilder(MobileDevice mobileDevice) {
        this.mobileDevice = mobileDevice;
        // http://appium.io/docs/en/writing-running-appium/caps/
        capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, NEW_COMMAND_TIMEOUT);
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, mobileDevice.getDevice().getName());
        capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, mobileDevice.getDevice().getSystemVersion());
        capabilities.setCapability(MobileCapabilityType.UDID, mobileDevice.getId());
        capabilities.setCapability(MobileCapabilityType.NO_RESET, true);
    }

    public DesiredCapabilities build() {
        return capabilities;
    }

    public DesiredCapabilitiesBuilder androidBasic() {
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
        capabilities.setCapability(AndroidMobileCapabilityType.UNICODE_KEYBOARD, true); // 切换到appium输入法

        if (((AndroidDevice) mobileDevice).greaterOrEqualsToAndroid5()) {
            capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2); // UIAutomation2 is only supported since Android 5.0 (Lollipop)
        } else {
            capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UIAutomator1");
        }

        capabilities.setCapability(AndroidMobileCapabilityType.SYSTEM_PORT, PortProvider.getUiautomator2ServerAvailablePort());
        capabilities.setCapability("chromedriverPort", PortProvider.getChromeDriverAvailablePort());
        // capabilities.setCapability("showChromedriverLog", true);
        Optional<String> chromedriverFilePath = ((AndroidDevice) mobileDevice).getChromedriverFilePath();
        if (chromedriverFilePath.isPresent()) {
            capabilities.setCapability(AndroidMobileCapabilityType.CHROMEDRIVER_EXECUTABLE, chromedriverFilePath.get());
        }

        return this;
    }

    public DesiredCapabilitiesBuilder androidApiDemos() {
        capabilities.setCapability("appPackage", "io.appium.android.apis");
        capabilities.setCapability("appActivity", "io.appium.android.apis.ApiDemos");
        capabilities.setCapability("autoLaunch", false);
        return this;
    }

    public DesiredCapabilitiesBuilder extractChromeAndroidPackageFromContextName() {
        capabilities.setCapability("extractChromeAndroidPackageFromContextName", true);
        return this;
    }

    public DesiredCapabilitiesBuilder androidWX() {
        capabilities.setCapability("appPackage", "com.tencent.mm");
        capabilities.setCapability("appActivity", "com.tencent.mm.ui.LauncherUI");
        return this;
    }

    public DesiredCapabilitiesBuilder androidWxTools() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setExperimentalOption("androidProcess", "com.tencent.mm:tools");
        chromeOptions.setCapability("browserName", "");
        capabilities.setCapability(AndroidMobileCapabilityType.CHROME_OPTIONS, chromeOptions);
        return this;
    }

    public DesiredCapabilitiesBuilder androidWxAppBrand() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setExperimentalOption("androidProcess", "com.tencent.mm:appbrand0");
        chromeOptions.setCapability("browserName", "");
        capabilities.setCapability(AndroidMobileCapabilityType.CHROME_OPTIONS, chromeOptions);
        return this;
    }

    public DesiredCapabilitiesBuilder androidSkip() {
        // skip可以提升初始化driver的速度
        capabilities.setCapability("skipServerInstallation", true);
        capabilities.setCapability("skipDeviceInitialization", true);
        capabilities.setCapability("skipUnlock", true);
        capabilities.setCapability("skipLogcatCapture", true);
        return this;
    }

    public DesiredCapabilitiesBuilder iOSBasic() {
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.IOS);
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.IOS_XCUI_TEST);
        capabilities.setCapability(IOSMobileCapabilityType.WDA_LOCAL_PORT, PortProvider.getWdaLocalAvailablePort());
        capabilities.setCapability("mjpegServerPort", PortProvider.getWdaMjpegServerAvailablePort());
        capabilities.setCapability(IOSMobileCapabilityType.WDA_STARTUP_RETRIES, 0);
        capabilities.setCapability("waitForQuiescence", false);
        capabilities.setCapability("skipLogCapture", true);
        // Get JSON source from WDA and parse into XML on Appium server. This can be much faster, especially on large devices.
        capabilities.setCapability("useJSONSource", true);
        // http://appium.io/docs/en/advanced-concepts/settings/
        capabilities.setCapability("mjpegServerFramerate", Integer.parseInt(App.getProperty("mjpegServerFramerate")));
        capabilities.setCapability("webkitDebugProxyPort", PortProvider.getWebkitDebugProxyAvalilablePort());

        // https://github.com/appium/appium-xcuitest-driver/blob/master/docs/real-device-config.md
        String xcodeOrgId = App.getProperty("xcodeOrgId");
        if (!StringUtils.isEmpty(xcodeOrgId)) {
            capabilities.setCapability("xcodeOrgId", xcodeOrgId);
        }
        String xcodeSigningId = App.getProperty("xcodeSigningId");
        if (!StringUtils.isEmpty(xcodeSigningId)) {
            capabilities.setCapability("xcodeSigningId", xcodeSigningId);
        }
        String updatedWDABundleId = App.getProperty("updatedWDABundleId");
        if (!StringUtils.isEmpty(updatedWDABundleId)) {
            capabilities.setCapability("updatedWDABundleId", updatedWDABundleId);
        }
        return this;
    }
}
