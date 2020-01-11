package com.daxiang.core.appium;

import com.daxiang.core.MobileDevice;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Created by jiangyitao.
 */
public class IosDriverBuilder implements AppiumDriverBuilder {

    @Override
    public AppiumDriver build(MobileDevice mobileDevice) {
        DesiredCapabilities capabilities = new DesiredCapabilitiesBuilder(mobileDevice).iOSBasic().build();
        return new IOSDriver(mobileDevice.getAppiumServer().getUrl(), capabilities);
    }
}
