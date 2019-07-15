package com.daxiang.action.appium.android;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class PressKey {

    private AppiumDriver driver;

    public PressKey(AppiumDriver driver) {
        this.driver = driver;
    }

    /**
     * @param code io.appium.java_client.android.nativekey.AndroidKey
     */
    public void excute(Object code) {
        Assert.notNull(code, "code不能为空");
        int _code = Integer.parseInt((String) code);

        if (!(driver instanceof AndroidDriver)) {
            throw new RuntimeException("AppiumDriver不是AndroidDriver");
        }

        AndroidDriver androidDriver = (AndroidDriver) driver;
        androidDriver.pressKeyCode(_code);
    }
}
