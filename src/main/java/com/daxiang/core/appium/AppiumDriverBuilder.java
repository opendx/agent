package com.daxiang.core.appium;

import com.daxiang.core.MobileDevice;
import io.appium.java_client.AppiumDriver;

/**
 * Created by jiangyitao.
 */
public interface AppiumDriverBuilder {
    AppiumDriver build(MobileDevice mobileDevice);
}
