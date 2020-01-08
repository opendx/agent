package com.daxiang.core.appium;

import com.daxiang.core.MobileDevice;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Created by jiangyitao.
 */
public class AndroidDriverBuilder implements AppiumDriverBuilder {

    @Override
    public AppiumDriver build(MobileDevice mobileDevice) {
        DesiredCapabilities capabilities = new DesiredCapabilitiesBuilder(mobileDevice)
                .androidBasic()
                .androidApiDemos()
                .androidSkip()
                .extractChromeAndroidPackageFromContextName()
                .build();
        return new AndroidDriver(mobileDevice.getAppiumServer().getUrl(), capabilities);
    }

    public AppiumDriver init(MobileDevice mobileDevice) {
        DesiredCapabilities capabilities = new DesiredCapabilitiesBuilder(mobileDevice)
                .androidBasic()
                .androidApiDemos()
                .build();
        return new AndroidDriver(mobileDevice.getAppiumServer().getUrl(), capabilities);
    }
}
