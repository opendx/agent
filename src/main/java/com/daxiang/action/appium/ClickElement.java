package com.daxiang.action.appium;

import org.openqa.selenium.WebElement;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class ClickElement {

    public WebElement excute(Object element) {
        Assert.notNull(element, "element不能为空");
        WebElement _element = (WebElement) element;
        _element.click();
        return _element;
    }
}
