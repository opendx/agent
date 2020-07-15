package com.daxiang.action;

import com.daxiang.core.Device;
import io.appium.java_client.MobileBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 * id 1 - 999
 * platforms = null
 */
public class BaseAction {

    public static final int EXECUTE_JAVA_CODE_ID = 1;

    protected Device device;

    public BaseAction(Device device) {
        this.device = device;
    }

    /**
     * 1.执行java代码
     *
     * @param code
     */
    public void executeJavaCode(String code) {
        Assert.hasText(code, "code不能为空");
    }

    /**
     * 2.休眠
     *
     * @param ms 毫秒
     * @throws InterruptedException
     */
    public void sleep(String ms) throws InterruptedException {
        Thread.sleep(parseLong(ms));
    }

    /**
     * 7.点击
     *
     * @param findBy
     * @param value
     * @return
     */
    public WebElement click(String findBy, String value) {
        WebElement element = findElement(findBy, value);
        element.click();
        return element;
    }

    /**
     * 8.查找元素
     *
     * @param findBy
     * @param value
     * @return
     */
    public WebElement findElement(String findBy, String value) {
        return device.getDriver().findElement(createBy(findBy, value));
    }

    /**
     * 9.查找元素
     *
     * @param findBy
     * @param value
     * @return 返回所有匹配的元素
     */
    public List<WebElement> findElements(String findBy, String value) {
        return device.getDriver().findElements(createBy(findBy, value));
    }

    /**
     * 10.sendKeys
     *
     * @param findBy
     * @param value
     * @param content
     * @return
     */
    public WebElement sendKeys(String findBy, String value, String content) {
        WebElement element = device.getDriver().findElement(createBy(findBy, value));
        if (content == null) {
            content = "";
        }
        element.sendKeys(content);
        return element;
    }

    /**
     * 11.设置隐式等待时间
     *
     * @param seconds
     */
    public void setImplicitlyWaitTime(String seconds) {
        device.getDriver().manage().timeouts().implicitlyWait(parseLong(seconds), TimeUnit.SECONDS);
    }

    /**
     * 12.等待元素可见
     *
     * @param findBy
     * @param value
     * @param timeoutInSeconds
     * @return
     */
    public WebElement waitForElementVisible(String findBy, String value, String timeoutInSeconds) {
        return new WebDriverWait(device.getDriver(), parseLong(timeoutInSeconds))
                .until(ExpectedConditions.visibilityOfElementLocated(createBy(findBy, value)));
    }

    /**
     * 13.等待元素在DOM里出现，不一定可见
     * 可用于检查toast是否显示
     *
     * @param findBy
     * @param value
     * @param timeoutInSeconds
     * @return
     */
    public WebElement waitForElementPresence(String findBy, String value, String timeoutInSeconds) {
        return new WebDriverWait(device.getDriver(), parseLong(timeoutInSeconds))
                .until(ExpectedConditions.presenceOfElementLocated(createBy(findBy, value)));
    }

    /**
     * 14.[web]访问url
     *
     * @param url
     */
    public void getUrl(String url) {
        Assert.hasText(url, "url不能为空");
        device.getDriver().get(url);
    }

    /**
     * 17. 元素是否显示
     *
     * @param findBy
     * @param value
     * @return
     */
    public boolean isElementDisplayed(String findBy, String value) {
        try {
            return isElementDisplayed(findElement(findBy, value));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 18. 元素是否显示
     *
     * @param element
     * @return
     */
    public boolean isElementDisplayed(WebElement element) {
        Assert.notNull(element, "element不能为空");

        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 19.等待元素可见
     */
    public WebElement waitForElementVisible(WebElement element, String timeoutInSeconds) {
        return new WebDriverWait(device.getDriver(), parseLong(timeoutInSeconds))
                .until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * 20.获取当前时间
     *
     * @param pattern
     * @return
     */
    public String now(String pattern) {
        if (StringUtils.isEmpty(pattern)) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    public String now() {
        return now("yyyy-MM-dd HH:mm:ss");
    }

    public By createBy(String findBy, String value) {
        Assert.hasText(findBy, "findBy不能为空");
        Assert.hasText(value, "value不能为空");

        switch (findBy) {
            case "id":
                return MobileBy.id(value);
            case "AccessibilityId":
                return MobileBy.AccessibilityId(value);
            case "xpath":
                return MobileBy.xpath(value);
            case "AndroidUIAutomator":
                // http://appium.io/docs/en/writing-running-appium/android/uiautomator-uiselector/
                return MobileBy.AndroidUIAutomator(value);
            case "iOSClassChain":
                return MobileBy.iOSClassChain(value);
            case "iOSNsPredicateString":
                // http://appium.io/docs/en/writing-running-appium/ios/ios-predicate/
                return MobileBy.iOSNsPredicateString(value);
            case "image":
                return MobileBy.image(value);
            case "className":
                return MobileBy.className(value);
            case "name":
                return MobileBy.name(value);
            case "cssSelector":
                return MobileBy.cssSelector(value);
            case "linkText":
                return MobileBy.linkText(value);
            case "partialLinkText":
                return MobileBy.partialLinkText(value);
            case "tagName":
                return MobileBy.tagName(value);
            default:
                throw new IllegalArgumentException("暂不支持: " + findBy);
        }
    }

    public boolean parseBoolean(String booleanString) {
        try {
            return Boolean.parseBoolean(booleanString);
        } catch (Exception e) {
            throw new IllegalArgumentException(booleanString + "必须为 true or false");
        }
    }

    public long parseLong(String longString) {
        try {
            return Long.parseLong(longString);
        } catch (Exception e) {
            throw new IllegalArgumentException(longString + "必须为数值");
        }
    }

    public int parseInt(String intString) {
        try {
            return Integer.parseInt(intString);
        } catch (Exception e) {
            throw new IllegalArgumentException(intString + "必须为数值");
        }
    }

}
