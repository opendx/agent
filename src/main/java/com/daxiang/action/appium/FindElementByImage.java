package com.daxiang.action.appium;

import io.appium.java_client.AppiumDriver;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class FindElementByImage {

    private AppiumDriver driver;

    public FindElementByImage(AppiumDriver driver) {
        this.driver = driver;
    }

    public WebElement excute(Object base64ImageTemplate) {
        Assert.notNull(base64ImageTemplate, "base64图片不能为空");
        String _base64ImageTemplate = StringUtils.substringAfter((String) base64ImageTemplate, "base64,");
        return driver.findElementByImage(_base64ImageTemplate);
    }
}
