package com.daxiang.action.appium;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;


/**
 * Created by jiangyitao.
 */
public class Click {

    private AppiumDriver driver;

    public Click(AppiumDriver driver) {
        this.driver = driver;
    }

    public WebElement excute(Object findBy, Object value) {
        WebElement element = new FindElement(driver).excute(findBy, value);
        element.click();
        return element;
    }
}
