package com.daxiang.core.appium;

import com.daxiang.core.MobileDevice;
import io.appium.java_client.AppiumDriver;

/**
 * Created by jiangyitao.
 */
abstract class AppiumDriverBuilder {
    private MobileDevice mobileDevice;

    public AppiumDriverBuilder(MobileDevice mobileDevice) {
        this.mobileDevice = mobileDevice;
    }

    public MobileDevice getMobileDevice() {
        return mobileDevice;
    }

    abstract AppiumDriver build();
}
