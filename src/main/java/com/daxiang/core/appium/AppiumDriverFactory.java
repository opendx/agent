package com.daxiang.core.appium;

import com.daxiang.core.MobileDevice;
import com.daxiang.core.Platform;
import com.daxiang.core.android.AndroidDevice;
import com.daxiang.core.ios.IosDevice;
import io.appium.java_client.AppiumDriver;

/**
 * Created by jiangyitao.
 */
public class AppiumDriverFactory {

    public static AppiumDriver create(MobileDevice mobileDevice, Integer platform) {
        switch (platform) {
            case Platform.ANDROID:
                return new AndroidDriverBuilder(mobileDevice).build();
            case Platform.IOS:
                return new IosDriverBuilder(mobileDevice).build();
            case Platform.ANDROID_WX_TOOLS:
                return new WxToolsDriverBuilder(mobileDevice).build();
            case Platform.ANDROID_WX_APP_BRAND:
                return new WxAppBrandDriverBuilder(mobileDevice).build();
            default:
                throw new IllegalArgumentException("不支持的platform: " + platform);
        }
    }

    public static AppiumDriver initAppiumDriver(MobileDevice mobileDevice) {
        if (mobileDevice instanceof AndroidDevice) {
            return new AndroidDriverBuilder(mobileDevice).init();
        } else if (mobileDevice instanceof IosDevice) {
            return new IosDriverBuilder(mobileDevice).build();
        }

        throw new IllegalArgumentException("不支持的设备: " + mobileDevice.getId());
    }
}
