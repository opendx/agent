package com.daxiang.core.appium;

import com.daxiang.core.MobileDevice;
import com.daxiang.core.Platform;
import io.appium.java_client.AppiumDriver;

/**
 * Created by jiangyitao.
 */
public class AppiumDriverFactory {

    public static AppiumDriver create(MobileDevice mobileDevice, Integer platform) {
        switch (platform) {
            case Platform.ANDROID:
                return new AndroidDriverBuilder().build(mobileDevice);
            case Platform.IOS:
                return new IosDriverBuilder().build(mobileDevice);
            case Platform.ANDROID_WX_TOOLS:
                return new WxToolsDriverBuilder().build(mobileDevice);
            case Platform.ANDROID_WX_APP_BRAND:
                return new WxAppBrandDriverBuilder().build(mobileDevice);
            default:
                throw new RuntimeException("不支持的platform: " + platform);
        }
    }
}
