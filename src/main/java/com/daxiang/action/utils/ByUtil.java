package com.daxiang.action.utils;

import io.appium.java_client.MobileBy;
import org.openqa.selenium.By;

/**
 * Created by jiangyitao.
 */
public class ByUtil {

    public static By getBy(String findBy, String value) {
        By by;
        switch (findBy) {
            case "id":
                by = MobileBy.id(value);
                break;
            case "AccessibilityId":
                by = MobileBy.AccessibilityId(value);
                break;
            case "xpath":
                by = MobileBy.xpath(value);
                break;
            case "AndroidUIAutomator":
                // http://appium.io/docs/en/writing-running-appium/android/uiautomator-uiselector/
                value = value.replaceAll("'", "\"");
                by = MobileBy.AndroidUIAutomator(value);
                break;
            case "iOSClassChain":
                by = MobileBy.iOSClassChain(value);
                break;
            case "iOSNsPredicateString":
                // http://appium.io/docs/en/writing-running-appium/ios/ios-predicate/
                by = MobileBy.iOSNsPredicateString(value);
                break;
            case "image":
                by = MobileBy.image(value);
                break;
            default:
                throw new RuntimeException("暂不支持: " + findBy);
        }
        return by;
    }
}
