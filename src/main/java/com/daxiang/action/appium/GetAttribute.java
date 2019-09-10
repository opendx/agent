package com.daxiang.action.appium;

import org.openqa.selenium.WebElement;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class GetAttribute {

    public String execute(Object element, Object attributeName) {
        Assert.notNull(element, "element不能为空");
        Assert.notNull(attributeName, "attributeName不能为空");

        WebElement _element = (WebElement) element;
        String _attributeName = (String) attributeName;

        return _element.getAttribute(_attributeName);
    }
}
