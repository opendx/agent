package com.daxiang.core.appium;

import com.daxiang.core.MobileDevice;
import com.daxiang.core.android.AndroidDevice;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;

/**
 * Created by jiangyitao.
 */
public class AndroidDriverBuilder implements AppiumDriverBuilder {

    @Override
    public AppiumDriver build(MobileDevice mobileDevice) {
        DesiredCapabilitiesBuilder builder = new DesiredCapabilitiesBuilder(mobileDevice)
                .androidBasic()
                .androidSkip()
                .extractChromeAndroidPackageFromContextName();
        handleApiDemos(mobileDevice, builder);
        return new AndroidDriver(mobileDevice.getAppiumServer().getUrl(), builder.build());
    }

    public AppiumDriver init(MobileDevice mobileDevice) {
        DesiredCapabilitiesBuilder builder = new DesiredCapabilitiesBuilder(mobileDevice)
                .androidBasic();
        handleApiDemos(mobileDevice, builder);
        return new AndroidDriver(mobileDevice.getAppiumServer().getUrl(), builder.build());
    }

    public void handleApiDemos(MobileDevice mobileDevice, DesiredCapabilitiesBuilder builder) {
        if (!((AndroidDevice) mobileDevice).greaterOrEqualsToAndroid5()) {
            builder.androidApiDemos(); // uiautomator1仍然需要指定app
        }
    }
}
