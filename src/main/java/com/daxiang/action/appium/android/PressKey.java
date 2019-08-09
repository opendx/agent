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
     * @param androidKeyCode io.appium.java_client.android.nativekey.AndroidKey
     */
    public void excute(Object androidKeyCode) {
        Assert.notNull(androidKeyCode, "androidKeyCode不能为空");
        int _androidKeyCode = Integer.parseInt((String) androidKeyCode);

        if (!(driver instanceof AndroidDriver)) {
            throw new RuntimeException("AppiumDriver不是AndroidDriver");
        }

        AndroidDriver androidDriver = (AndroidDriver) driver;
        androidDriver.pressKeyCode(_androidKeyCode);
    }
}
