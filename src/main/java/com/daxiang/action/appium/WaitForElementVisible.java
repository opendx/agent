package com.daxiang.action.appium;

import com.daxiang.action.utils.ByUtil;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class WaitForElementVisible {

    private AppiumDriver driver;

    public WaitForElementVisible(AppiumDriver driver) {
        this.driver = driver;
    }

    public WebElement execute(Object findBy, Object value, Object maxWaitTimeInSeconds) {
        Assert.notNull(findBy, "findBy不能为空");
        Assert.notNull(value, "value不能为空");
        Assert.notNull(maxWaitTimeInSeconds, "最大等待时间不能为空");

        String _findBy = (String) findBy;
        String _value = (String) value;
        long _maxWaitTimeInSeconds = Long.parseLong((String) maxWaitTimeInSeconds);

        return new WebDriverWait(driver, _maxWaitTimeInSeconds).until(ExpectedConditions.visibilityOfElementLocated(ByUtil.getBy(_findBy, _value)));
    }
}
