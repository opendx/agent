package com.daxiang.action.appium;

import com.daxiang.action.utils.ByUtil;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class SendKeys {

    private AppiumDriver driver;

    public SendKeys(AppiumDriver driver) {
        this.driver = driver;
    }

    public WebElement execute(Object findBy, Object value, Object content) {
        Assert.notNull(findBy, "findBy不能为空");
        Assert.notNull(value, "value不能为空");
        Assert.notNull(content, "content不能为空");

        String _findBy = (String) findBy;
        String _value = (String) value;
        String _content = (String) content;

        WebElement element = driver.findElement(ByUtil.getBy(_findBy, _value));
        element.sendKeys(_content);
        return element;
    }
}
