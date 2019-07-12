package com.daxiang.action;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.springframework.util.Assert;


/**
 * Created by jiangyitao.
 */
public class Click {

    private AppiumDriver driver;

    public Click(AppiumDriver driver) {
        this.driver = driver;
    }

    public void excute(Object findBy, Object value) throws Exception {
        Assert.notNull(findBy, "findBy不能为空");
        Assert.notNull(value, "value不能为空");

        String _findBy = (String) findBy;
        String _value = (String) value;

        By by;
        // todo xx
        switch (_findBy) {
            case "id":
                by = By.id(_value);
                break;
            case "name":
                by = By.name(_value);
                break;
            case "className":
                by = By.className(_value);
                break;
            case "xpath":
                by = By.xpath(_value);
                break;
            default:
                throw new RuntimeException("暂不支持：" + _findBy);
        }

        driver.findElement(by).click();
    }
}
