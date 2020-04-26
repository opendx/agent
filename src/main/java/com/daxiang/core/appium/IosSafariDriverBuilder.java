package com.daxiang.core.appium;

import com.daxiang.core.MobileDevice;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Created by jiangyitao.
 */
class IosSafariDriverBuilder extends AppiumDriverBuilder {

    public IosSafariDriverBuilder(MobileDevice mobileDevice) {
        super(mobileDevice);
    }

    @Override
    public AppiumDriver build() {
        MobileDevice mobileDevice = getMobileDevice();

        DesiredCapabilities capabilities = new DesiredCapabilitiesBuilder(mobileDevice)
                .iOSBasic()
                .iosSafari()
                .build();
        return new IOSDriver(mobileDevice.getAppiumServer().getUrl(), capabilities);
    }
}
