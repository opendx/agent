package com.daxiang.action;

import com.daxiang.core.action.annotation.Action;
import com.daxiang.core.action.annotation.Param;
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
    public static final String FIND_BY_POSSIBLE_VALUES = "[" +
            "{'value':'id','description':'By.id'}," +
            "{'value':'AccessibilityId','description':'By.AccessibilityId'}," +
            "{'value':'xpath','description':'By.xpath'}," +
            "{'value':'AndroidUIAutomator','description':'By.AndroidUIAutomator'}," +
            "{'value':'iOSClassChain','description':'By.iOSClassChain'}," +
            "{'value':'iOSNsPredicateString','description':'By.iOSNsPredicateString'}," +
            "{'value':'image','description':'By.image'}," +
            "{'value':'className','description':'By.className'}," +
            "{'value':'name','description':'By.name'}," +
            "{'value':'cssSelector','description':'By.cssSelector'}," +
            "{'value':'linkText','description':'By.linkText'}," +
            "{'value':'partialLinkText','description':'By.partialLinkText'}," +
            "{'value':'tagName','description':'By.tagName'}" +
            "]";

    protected Device device;

    public BaseAction(Device device) {
        this.device = device;
    }

    @Action(id = EXECUTE_JAVA_CODE_ID, name = "执行java代码")
    public void executeJavaCode(@Param(description = "java代码") String code) {
        Assert.hasText(code, "code不能为空");
    }

    @Action(id = 2, name = "休眠")
    public void sleep(@Param(description = "休眠时长，单位：毫秒") String ms) throws InterruptedException {
        Thread.sleep(parseLong(ms));
    }

    @Action(id = 7, name = "点击")
    public WebElement click(@Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy, String value) {
        WebElement element = findElement(findBy, value);
        element.click();
        return element;
    }

    @Action(id = 8, name = "查找元素")
    public WebElement findElement(@Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy, String value) {
        return device.getDriver().findElement(createBy(findBy, value));
    }

    @Action(id = 9, name = "查找元素列表")
    public List<WebElement> findElements(@Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy, String value) {
        return device.getDriver().findElements(createBy(findBy, value));
    }

    @Action(id = 10, name = "输入")
    public WebElement sendKeys(@Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy, String value,
                               @Param(description = "输入内容") String content) {
        WebElement element = device.getDriver().findElement(createBy(findBy, value));
        if (content == null) {
            content = "";
        }
        element.sendKeys(content);
        return element;
    }

    @Action(id = 11, name = "设置隐式等待时间")
    public void setImplicitlyWaitTime(@Param(description = "隐式等待时间，单位：秒") String seconds) {
        device.getDriver().manage().timeouts().implicitlyWait(parseLong(seconds), TimeUnit.SECONDS);
    }

    @Action(id = 12, name = "等待元素可见")
    public WebElement waitForElementVisible(@Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy, String value,
                                            @Param(description = "最大等待时间，单位：秒") String timeoutInSeconds) {
        return new WebDriverWait(device.getDriver(), parseLong(timeoutInSeconds))
                .until(ExpectedConditions.visibilityOfElementLocated(createBy(findBy, value)));
    }

    @Action(id = 13, name = "等待元素出现", description = "等待元素在DOM里出现，不一定可见。移动端可用于检测toast")
    public WebElement waitForElementPresence(@Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy, String value,
                                             @Param(description = "最大等待时间，单位：秒") String timeoutInSeconds) {
        return new WebDriverWait(device.getDriver(), parseLong(timeoutInSeconds))
                .until(ExpectedConditions.presenceOfElementLocated(createBy(findBy, value)));
    }

    @Action(id = 14, name = "[web]访问url")
    public void getUrl(String url) {
        Assert.hasText(url, "url不能为空");
        device.getDriver().get(url);
    }

    @Action(id = 17, name = "元素是否显示")
    public boolean isElementDisplayed(@Param(description = "查找方式", possibleValues = FIND_BY_POSSIBLE_VALUES) String findBy, String value) {
        try {
            return isElementDisplayed(findElement(findBy, value));
        } catch (Exception e) {
            return false;
        }
    }

    @Action(id = 18, name = "元素是否显示")
    public boolean isElementDisplayed(WebElement element) {
        Assert.notNull(element, "element不能为空");

        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    @Action(id = 19, name = "等待元素可见")
    public WebElement waitForElementVisible(WebElement element, @Param(description = "最大等待时间，单位：秒") String timeoutInSeconds) {
        return new WebDriverWait(device.getDriver(), parseLong(timeoutInSeconds))
                .until(ExpectedConditions.visibilityOf(element));
    }

    @Action(id = 20, name = "获取当前时间")
    public String now(@Param(description = "默认yyyy-MM-dd HH:mm:ss") String pattern) {
        if (StringUtils.isEmpty(pattern)) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    public String now() {
        return now("yyyy-MM-dd HH:mm:ss");
    }

    @Action(id = 21, name = "accept对话框")
    public boolean acceptAlert() {
        return device.acceptAlert();
    }

    @Action(id = 22, name = "异步accept对话框")
    public void asyncAcceptAlert(@Param(description = "超时时间，单位：秒") String timeoutInSeconds,
                                 @Param(description = "是否只处理一次, true or false") String once) {
        boolean _once = parseBoolean(once);
        long timeoutInMs = parseLong(timeoutInSeconds) * 1000;

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < timeoutInMs) {
                if (_once && acceptAlert()) {
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {

                }
            }
        }).start();
    }

    @Action(id = 23, name = "dismiss对话框")
    public boolean dismissAlert() {
        return device.dismissAlert();
    }

    @Action(id = 24, name = "异步dismiss对话框")
    public void asyncDismissAlert(@Param(description = "超时时间，单位：秒") String timeoutInSeconds,
                                  @Param(description = "是否只处理一次, true or false") String once) {
        boolean _once = parseBoolean(once);
        long timeoutInMs = parseLong(timeoutInSeconds) * 1000;

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < timeoutInMs) {
                if (_once && dismissAlert()) {
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {

                }
            }
        }).start();
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
