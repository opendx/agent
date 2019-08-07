package com.daxiang.core.appium;

import com.daxiang.core.MobileDevice;
import com.daxiang.core.PortProvider;
import com.daxiang.core.ios.IosUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.MobilePlatform;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Created by jiangyitao.
 */
public class IosDriverBuilder implements AppiumDriverBuilder {

    private static final String BUNDLE_ID = "com.apple.mobilesafari";

    @Override
    public AppiumDriver build(MobileDevice mobileDevice) {
        DesiredCapabilities capabilities = createDesiredCapabilities(mobileDevice);
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.IOS);
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.IOS_XCUI_TEST);
        capabilities.setCapability(IOSMobileCapabilityType.WDA_LOCAL_PORT, PortProvider.getWdaLocalAvailablePort());
        capabilities.setCapability("mjpegServerPort", PortProvider.getWdaMjpegServerAvailablePort());
        capabilities.setCapability(IOSMobileCapabilityType.BUNDLE_ID, BUNDLE_ID);
        capabilities.setCapability("waitForQuiescence", false);
        capabilities.setCapability("skipLogCapture", true);
//            capabilities.setCapability("useJSONSource", true); // https://github.com/appium/appium-xcuitest-driver#desired-capabilities
        IOSDriver iosDriver = new IOSDriver(mobileDevice.getAppiumServer().getUrl(), capabilities);
        IosUtil.pressHome(iosDriver);
        return iosDriver;
    }
}
