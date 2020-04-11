package com.daxiang.core.appium;

import com.daxiang.core.MobileDevice;
import com.daxiang.core.android.AndroidDevice;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;

/**
 * Created by jiangyitao.
 */
class AndroidDriverBuilder extends AppiumDriverBuilder {

    public AndroidDriverBuilder(MobileDevice mobileDevice) {
        super(mobileDevice);
    }

    @Override
    public AppiumDriver build() {
        MobileDevice mobileDevice = getMobileDevice();

        DesiredCapabilitiesBuilder builder = new DesiredCapabilitiesBuilder(mobileDevice)
                .androidBasic()
                .androidSkip()
                .extractChromeAndroidPackageFromContextName();

        if (!((AndroidDevice) mobileDevice).greaterOrEqualsToAndroid5()) {
            builder.androidApiDemos(); // uiautomator1仍然需要指定app
        }

        return new AndroidDriver(mobileDevice.getAppiumServer().getUrl(), builder.build());
    }

    public AppiumDriver init() {
        MobileDevice mobileDevice = getMobileDevice();

        DesiredCapabilitiesBuilder builder = new DesiredCapabilitiesBuilder(mobileDevice)
                .androidBasic();

        if (!((AndroidDevice) mobileDevice).greaterOrEqualsToAndroid5()) {
            builder.androidApiDemos(); // uiautomator1仍然需要指定app
        }

        return new AndroidDriver(mobileDevice.getAppiumServer().getUrl(), builder.build());
    }
}
