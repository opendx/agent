package com.daxiang.action.utils;

import org.openqa.selenium.By;

/**
 * Created by jiangyitao.
 */
public class ByUtil {

    public static By getBy(String findBy, String value) {
        By by;
        switch (findBy) {
            case "id":
                by = By.id(value);
                break;
            case "xpath":
                by = By.xpath(value);
                break;
            case "name":
                by = By.name(value);
                break;
            case "className":
                by = By.className(value);
                break;
            default:
                throw new RuntimeException("暂不支持：" + findBy);
        }
        return by;
    }
}
