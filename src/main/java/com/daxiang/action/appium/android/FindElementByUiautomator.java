package com.daxiang.action.appium.android;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class FindElementByUiautomator {

    private AppiumDriver driver;

    public FindElementByUiautomator(AppiumDriver driver) {
        this.driver = driver;
    }

    public WebElement excute(Object uiautomator) {
        Assert.notNull(uiautomator, "uiautomator不能为空");
        String _uiautomator = (String) uiautomator;
        _uiautomator = _uiautomator.replaceAll("'", "\"");

        if(!(driver instanceof AndroidDriver)) {
            throw new RuntimeException("AppiumDriver非AndroidDriver");
        }

        AndroidDriver androidDriver = (AndroidDriver) driver;
        return androidDriver.findElementByAndroidUIAutomator(_uiautomator);
    }
}
