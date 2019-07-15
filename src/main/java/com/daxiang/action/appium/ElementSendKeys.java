package com.daxiang.action.appium;

import org.openqa.selenium.WebElement;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class ElementSendKeys {

    public WebElement excute(Object webElement, Object content) {
        Assert.notNull(webElement, "webElement不能为空");
        Assert.notNull(content, "content不能为空");

        WebElement _webElement = (WebElement) webElement;
        String _content = (String) content;

        _webElement.sendKeys(_content);
        return _webElement;
    }
}
