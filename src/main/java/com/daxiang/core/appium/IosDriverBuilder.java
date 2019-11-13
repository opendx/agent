package com.daxiang.core.appium;

import com.daxiang.App;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.PortProvider;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.remote.MobilePlatform;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.util.StringUtils;

/**
 * Created by jiangyitao.
 */
public class IosDriverBuilder implements AppiumDriverBuilder {

    @Override
    public AppiumDriver build(MobileDevice mobileDevice, boolean isFirstBuild) {
        // https://github.com/appium/appium-xcuitest-driver
        DesiredCapabilities capabilities = createDesiredCapabilities(mobileDevice);
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, MobilePlatform.IOS);
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.IOS_XCUI_TEST);
        capabilities.setCapability(IOSMobileCapabilityType.WDA_LOCAL_PORT, PortProvider.getWdaLocalAvailablePort());
        capabilities.setCapability(IOSMobileCapabilityType.WDA_STARTUP_RETRY_INTERVAL, 60000);
        capabilities.setCapability("mjpegServerPort", PortProvider.getWdaMjpegServerAvailablePort());
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

        return new IOSDriver(mobileDevice.getAppiumServer().getUrl(), capabilities);
    }
}
