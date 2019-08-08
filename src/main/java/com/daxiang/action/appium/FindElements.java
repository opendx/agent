package com.daxiang.action.appium;

import com.daxiang.action.utils.ByUtil;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by jiangyitao.
 */
public class FindElements {

    private AppiumDriver driver;

    public FindElements(AppiumDriver driver) {
        this.driver = driver;
    }

    public List<WebElement> excute(Object findBy, Object value) {
        Assert.notNull(findBy, "findBy不能为空");
        Assert.notNull(value, "value不能为空");

        String _findBy = (String) findBy;
        String _value = (String) value;

        return driver.findElements(ByUtil.getBy(_findBy,_value));
    }
}
